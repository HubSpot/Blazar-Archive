package com.hubspot.blazar.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDiscoveryResult;
import com.hubspot.blazar.config.BlazarConfigurationWrapper;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.discovery.ModuleDiscoveryHandler;
import com.hubspot.blazar.guice.BaseCommandModule;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

public class VersionBackFillCommand extends ConfiguredCommand<BlazarConfigurationWrapper> {
  private static String COMMAND_NAME = "version_backfill";
  private static String COMMAND_DESC = "Finds projects with no version data for their dependencies, and updates their versions";
  private static final Logger LOG = LoggerFactory.getLogger(VersionBackFillCommand.class);
  private final ExecutorService executorService;

  @Inject
  public VersionBackFillCommand() {
    super(COMMAND_NAME, COMMAND_DESC);
    this.executorService = Executors.newFixedThreadPool(10);
  }

  @Override
  protected void run(Bootstrap<BlazarConfigurationWrapper> bootstrap,
                     Namespace namespace,
                     BlazarConfigurationWrapper configuration) throws Exception {
    Injector injector = Guice.createInjector(new BaseCommandModule(bootstrap, configuration));

    try {
      ModuleDiscoveryHandler moduleDiscoveryHandler = injector.getInstance(ModuleDiscoveryHandler.class);
      DependenciesService dependenciesService = injector.getInstance(DependenciesService.class);

      Set<GitInfo> toBeReDiscovered = dependenciesService.getBranchesWithNonVersionedDependencies();
      Set<GitInfo> failed = new HashSet<>();
      Map<GitInfo, Future<Boolean>> futureMap = new HashMap<>();
      for (GitInfo branch : toBeReDiscovered) {
        Task task = new Task(branch, moduleDiscoveryHandler);
        Future<Boolean> future = executorService.submit(task);
        futureMap.put(branch, future);
      }

      for (Map.Entry<GitInfo, Future<Boolean>> entry : futureMap.entrySet()) {
        handleResult(failed, entry.getKey(), entry.getValue());
      }

      LOG.info("Failed to process the following branches {}", failed);
    } finally {
      executorService.shutdownNow();
    }
  }

  private class Task implements Callable<Boolean> {

    private final GitInfo branch;
    private final ModuleDiscoveryHandler moduleDiscoveryHandler;

    Task(GitInfo branch, ModuleDiscoveryHandler moduleDiscoveryHandler) {
      this.branch = branch;
      this.moduleDiscoveryHandler = moduleDiscoveryHandler;
    }

    @Override
    public Boolean call() throws Exception {
      LOG.info("Starting to process gitInfo {}", branch.toString());
      try {
        ModuleDiscoveryResult result = moduleDiscoveryHandler.updateModules(branch, true);
        LOG.info("Got Discovery result: {}", result);
        return true;
      } catch (IOException e) {
        LOG.info("Failed to discover branch {}", branch.toString(), e);
        return false;
      }
    }
  }

  private void handleResult(Set<GitInfo> failed, GitInfo branch, Future<Boolean> future) {
    boolean success;
    LOG.info("Collecting future for branch {}", branch.toString());
    try {
      success = future.get();
    } catch (Exception e) {
      success = false;
      LOG.error("Exception while processing branch {}", branch, e);
    }

    if (success) {
      LOG.info("Completed re-discovery of {}", branch.toString());
    } else {
      LOG.info("Failed to process branch {}", branch.toString());
      failed.add(branch);
    }
  }
}
