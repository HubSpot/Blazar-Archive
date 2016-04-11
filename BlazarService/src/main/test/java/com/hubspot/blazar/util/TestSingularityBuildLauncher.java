package com.hubspot.blazar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.singularity.client.SingularityClient;

public class TestSingularityBuildLauncher extends SingularityBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(TestSingularityBuildLauncher.class);
  private final ModuleBuildResource moduleBuildResource;

  @Inject
  public TestSingularityBuildLauncher(SingularityClient singularityClient,
                                      BlazarConfiguration blazarConfiguration,
                                      ModuleBuildResource moduleBuildResource) {
    super(singularityClient, blazarConfiguration);
    this.moduleBuildResource = moduleBuildResource;
  }


  @Override
  public synchronized void launchBuild(ModuleBuild build) throws Exception {
    LOG.debug("Pretending to launch {} calling start", build);
    ModuleBuild inProgress = moduleBuildResource.start(build.getId().get(), Optional.of(build.toString()));
    LOG.debug("Build {} now in progress, publishing success", inProgress);
    ModuleBuild success = moduleBuildResource.completeSuccess(inProgress.getId().get());
    LOG.info("Build {} succeed", success);
  }
}
