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

import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.discovery.CompositeModuleDiscovery;
import com.hubspot.blazar.guice.BaseCommandModule;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

public class VersionBackFillCommand extends ConfiguredCommand<BlazarConfiguration> {
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
  protected void run(Bootstrap<BlazarConfiguration> bootstrap,
                     Namespace namespace,
                     BlazarConfiguration configuration) throws Exception {
    Injector injector = Guice.createInjector(new BaseCommandModule(bootstrap, configuration));

    try {
      CompositeModuleDiscovery compositeModuleDiscovery = injector.getInstance(CompositeModuleDiscovery.class);
      DependenciesService dependenciesService = injector.getInstance(DependenciesService.class);
      ModuleDiscoveryService moduleDiscoveryService = injector.getInstance(ModuleDiscoveryService.class);

      Set<GitInfo> toBeReDiscovered = dependenciesService.getBranchesWithNonVersionedDependencies();
      Set<GitInfo> failed = new HashSet<>();
      Map<GitInfo, Future<Boolean>> futureMap = new HashMap<>();
      for (GitInfo branch : toBeReDiscovered) {
        Task task = new Task(branch, compositeModuleDiscovery, moduleDiscoveryService);
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
    private final CompositeModuleDiscovery compositeModuleDiscovery;
    private final ModuleDiscoveryService moduleDiscoveryService;

    Task(GitInfo branch, CompositeModuleDiscovery compositeModuleDiscovery, ModuleDiscoveryService moduleDiscoveryService) {
      this.branch = branch;
      this.compositeModuleDiscovery = compositeModuleDiscovery;
      this.moduleDiscoveryService = moduleDiscoveryService;
    }

    @Override
    public Boolean call() throws Exception {
      LOG.info("Starting to process gitInfo {}", branch.toString());
      try {
        DiscoveryResult result = compositeModuleDiscovery.discover(branch);
        LOG.info("Got Discovery result: {}", result);
        Set<Module> modules = moduleDiscoveryService.handleDiscoveryResult(branch, result);
        LOG.info("Discovered modules {}", modules);
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
