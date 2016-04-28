package com.hubspot.blazar.util;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.singularity.client.SingularityClient;

@Singleton
public class TestSingularityBuildLauncher extends SingularityBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(TestSingularityBuildLauncher.class);
  private final ModuleBuildService moduleBuildService;
  private final ModuleBuildResource moduleBuildResource;
  private Set<Integer> failingModules;


  @Inject
  public TestSingularityBuildLauncher(SingularityClient singularityClient,
                                      BlazarConfiguration blazarConfiguration,
                                      ModuleBuildService moduleBuildService,
                                      ModuleBuildResource moduleBuildResource) {
    super(singularityClient, blazarConfiguration);
    this.moduleBuildService = moduleBuildService;
    this.moduleBuildResource = moduleBuildResource;
    this.failingModules = ImmutableSet.of();
  }


  public void clearModulesToFail() {
    failingModules = ImmutableSet.of();
  }

  public void setModulesToFail(Set<Integer> modules) {
    failingModules = ImmutableSet.copyOf(modules);
  }

  @Override
  public synchronized void launchBuild(ModuleBuild build) throws Exception {
    build = moduleBuildService.get(build.getId().get()).get();
    if (build.getState().isWaiting()) {
      return;
    }
    if (failingModules.contains(build.getModuleId())) {
      failBuild(build);
    } else {
      passBuild(build);
    }
  }

  private void failBuild(ModuleBuild build) {
    LOG.info("Pretending to launch {} calling start", build);
    ModuleBuild inProgress = moduleBuildResource.start(build.getId().get(), Optional.of(build.toString()));
    LOG.info("Build {} now in progress, publishing failure", inProgress);
    ModuleBuild failure = moduleBuildResource.completeFailure(inProgress.getId().get());
    LOG.info("Build {} Failed", failure);
  }

  private void passBuild(ModuleBuild build) {
    LOG.info("Pretending to launch {} calling start", build);
    ModuleBuild inProgress = moduleBuildResource.start(build.getId().get(), Optional.of(build.toString()));
    LOG.info("Build {} now in progress, publishing success", inProgress);
    ModuleBuild success = moduleBuildResource.completeSuccess(inProgress.getId().get());
    LOG.info("Build {} succeed", success);
  }

}
