package com.hubspot.blazar.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.guice.BaseCommandModule;
import com.hubspot.blazar.util.GitHubHelper;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

public class CleanRepoMetadataCommand extends ConfiguredCommand<BlazarConfiguration> {
  private static final String COMMAND_NAME = "clean_missing_repos";
  private static final String COMMAND_DESC = "Finds repos no longer in the managed organizations and marks all branches as inactive";
  private static final Logger LOG = LoggerFactory.getLogger(CleanRepoMetadataCommand.class);
  private final ExecutorService executorService;

  public CleanRepoMetadataCommand() {
    super(COMMAND_NAME, COMMAND_DESC);
    this.executorService = Executors.newFixedThreadPool(10);
  }

  @Override
  protected void run(Bootstrap<BlazarConfiguration> bootstrap,
                     Namespace namespace,
                     BlazarConfiguration configuration) throws Exception {
    Injector injector = Guice.createInjector(new BaseCommandModule(bootstrap, configuration));
    try {
      GitHubHelper gitHubHelper = injector.getInstance(GitHubHelper.class);
      BranchService branchService = injector.getInstance(BranchService.class);
      Set<GitInfo> branches = branchService.getAllActive();
      Map<GitInfo, CompletableFuture<Void>> futures = new HashMap<>();

      for (GitInfo branch : branches) {
        futures.put(branch, CompletableFuture.runAsync(new GitBranchUpdater(branch, gitHubHelper, configuration, branchService), executorService));
      }

      for(Map.Entry<GitInfo, CompletableFuture<Void>> futureEntry : futures.entrySet()) {
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
