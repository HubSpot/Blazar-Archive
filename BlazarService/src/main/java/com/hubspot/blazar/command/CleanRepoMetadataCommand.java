package com.hubspot.blazar.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarWrapperConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.guice.BaseCommandModule;
import com.hubspot.blazar.util.GitHubHelper;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

public class CleanRepoMetadataCommand extends ConfiguredCommand<BlazarWrapperConfiguration> {
  private static final String COMMAND_NAME = "clean_repo_metadata";
  private static final String COMMAND_DESC = "Finds repos no longer in the managed organizations and marks all branches as inactive";
  private static final Logger LOG = LoggerFactory.getLogger(CleanRepoMetadataCommand.class);
  private static final String NOOP_FLAG = "noop";
  private final ExecutorService executorService;

  public CleanRepoMetadataCommand() {
    super(COMMAND_NAME, COMMAND_DESC);

    final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("CleanupWorker-%d").setDaemon(true).build();
    this.executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(3000), threadFactory);
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
    subparser.addArgument("-n", "--noop")
        .dest(NOOP_FLAG)
        .type(Boolean.class)
        .help("Runs in noop mode; makes no changes to the database")
        .setDefault(false);
  }

  @Override
  protected void run(
      Bootstrap<BlazarWrapperConfiguration> bootstrap,
      Namespace namespace,
      BlazarWrapperConfiguration configuration) throws Exception {

    boolean noop = namespace.getBoolean(NOOP_FLAG);
    Injector injector = Guice.createInjector(new BaseCommandModule(bootstrap, configuration));
    try {
      GitHubHelper gitHubHelper = injector.getInstance(GitHubHelper.class);
      BranchService branchService = injector.getInstance(BranchService.class);
      Stack<GitInfo> branchStack = new Stack<>();
      branchStack.addAll(branchService.getAllActive());
      Map<GitInfo, CompletableFuture<Void>> futures = new HashMap<>();

      LOG.info("Starting cleanup of {} active branches", branchStack.size());

      while (!branchStack.isEmpty()) {
        GitInfo branch = branchStack.pop();
        GitBranchUpdater updater = new GitBranchUpdater(branch, gitHubHelper, configuration.getBlazarConfiguration(), branchService, noop);
        try {
          futures.put(branch, CompletableFuture.runAsync(updater, executorService));
        } catch (RejectedExecutionException e) {
          try {
            LOG.debug("Execution rejected for {} waiting to re-submit", branch);
            Thread.sleep(1500);
            branchStack.push(branch);
          } catch (InterruptedException ie) {
            LOG.info("Interrupted while waiting for queue to drain");
            throw e;
          }
        }
      }

      for (Map.Entry<GitInfo, CompletableFuture<Void>> futureEntry : futures.entrySet()) {
        try {
          futureEntry.getValue().get();
        } catch (ExecutionException e) {
          LOG.error("Got exception while processing {}", futureEntry.getKey(), e);
        } catch (InterruptedException e) {
          LOG.error("Was interrupted while processing {}", futureEntry.getKey(), e);
        }
      }
    } finally {
      executorService.shutdownNow();
    }
  }
}
