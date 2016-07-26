package com.hubspot.blazar.guice;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

public class VersionBackFillCommand extends ConfiguredCommand<BlazarConfiguration> {
  private static String COMMAND_NAME = "version_backfill";
  private static String COMMAND_DESC = "Finds projects with no version data for their dependencies, and updates their versions";
  private static final Logger LOG = LoggerFactory.getLogger(VersionBackFillCommand.class);

  @Inject
  public VersionBackFillCommand() {
    super(COMMAND_NAME, COMMAND_DESC);
  }

  @Override
  protected void run(Bootstrap<BlazarConfiguration> bootstrap,
                     Namespace namespace,
                     BlazarConfiguration configuration) throws Exception {

    Injector i = Guice.createInjector(new VersionBackFillCommandModule(bootstrap, configuration));

    CompositeModuleDiscovery compositeModuleDiscovery = i.getInstance(CompositeModuleDiscovery.class);
    DependenciesService dependenciesService = i.getInstance(DependenciesService.class);
    ModuleDiscoveryService moduleDiscoveryService = i.getInstance(ModuleDiscoveryService.class);

    Set<GitInfo> toBeReDiscovered = dependenciesService.getBranchesWithNonVersionedDependencies();
    Set<GitInfo> failed = new HashSet<>();
    for (GitInfo branch : toBeReDiscovered) {
      LOG.info("Starting to process gitInfo {}", branch.toString());
      boolean success;
      try {
        success = discover(branch, compositeModuleDiscovery, moduleDiscoveryService);
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
    LOG.info("Failed to process the following branches {}", failed);
  }

  private boolean discover(GitInfo gitinfo, CompositeModuleDiscovery compositeModuleDiscovery, ModuleDiscoveryService moduleDiscoveryService) {
    try {
      DiscoveryResult result = compositeModuleDiscovery.discover(gitinfo);
      LOG.info("Got Discovery result: {}", result);
      Set<Module> modules = moduleDiscoveryService.handleDiscoveryResult(gitinfo, result);
      LOG.info("Discovered modules {}", modules);
      return true;
    } catch (IOException e) {
      LOG.info("Failed to discover branch {}", gitinfo.toString(), e);
      return false;
    }
  }
}
