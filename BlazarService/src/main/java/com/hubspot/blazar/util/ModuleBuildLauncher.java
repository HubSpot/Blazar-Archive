package com.hubspot.blazar.util;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.externalservice.BuildClusterService;

@Singleton
public class ModuleBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleBuildLauncher.class);

  private final ModuleBuildService moduleBuildService;
  private BuildClusterService buildClusterService;

  @Inject
  public ModuleBuildLauncher(ModuleBuildService moduleBuildService,
                             BuildClusterService buildClusterService) {
    this.moduleBuildService = moduleBuildService;
    this.buildClusterService = buildClusterService;
  }

  public void launch(ModuleBuild build) throws Exception {
    try {
      LOG.info("About to launch docker container for build {}", build);
      buildClusterService.launchBuildContainer(build);
    } catch (Exception e) {
      throw new NonRetryableBuildException(String.format("An error occurred while launching docker container for build %s. Will not retry the build", build), e);
    }

    ModuleBuild launching = build.toBuilder()
        .setStartTimestamp(Optional.of(System.currentTimeMillis()))
        .setState(State.LAUNCHING)
        .build();

    LOG.info("Updating status of Module Build {} to {}", launching.getId().get(), launching.getState());
    moduleBuildService.begin(launching);
  }


}
