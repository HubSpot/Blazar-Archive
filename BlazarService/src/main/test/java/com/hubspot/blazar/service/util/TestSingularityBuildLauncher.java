package com.hubspot.blazar.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.singularity.client.SingularityClient;

public class TestSingularityBuildLauncher extends SingularityBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(TestSingularityBuildLauncher.class);
  private final ModuleBuildService moduleBuildService;
  private final ModuleBuildResource moduleBuildResource;


  @Inject
  public TestSingularityBuildLauncher(SingularityClient singularityClient,
                                      BlazarConfiguration blazarConfiguration,
                                      ModuleBuildService moduleBuildService,
                                      ModuleBuildResource moduleBuildResource) {
    super(singularityClient, blazarConfiguration);
    this.moduleBuildService = moduleBuildService;
    this.moduleBuildResource = moduleBuildResource;
  }


  @Override
  public synchronized void launchBuild(ModuleBuild build) throws Exception {
    build = moduleBuildService.get(build.getId().get()).get();
    if (build.getState().isWaiting()) {
      return;
    }
    LOG.info("Pretending to launch {} calling start", build);
    ModuleBuild inProgress = moduleBuildResource.start(build.getId().get(), Optional.of(build.toString()));
    LOG.info("Build {} now in progress, publishing success", inProgress);
    ModuleBuild success = moduleBuildResource.completeSuccess(inProgress.getId().get());
    LOG.info("Build {} succeed", success);
  }
}
