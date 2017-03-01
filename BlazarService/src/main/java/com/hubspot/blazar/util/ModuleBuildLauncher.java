package com.hubspot.blazar.util;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.data.service.ModuleBuildService;

@Singleton
public class ModuleBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleBuildLauncher.class);

  private final ModuleBuildService moduleBuildService;

  @Inject
  public ModuleBuildLauncher(ModuleBuildService moduleBuildService) {
    this.moduleBuildService = moduleBuildService;
  }

  public void launch(ModuleBuild build) throws Exception {
    ModuleBuild launching = build.toBuilder()
        .setStartTimestamp(Optional.of(System.currentTimeMillis()))
        .setState(State.LAUNCHING)
        .build();

    LOG.info("Updating status of Module Build {} to {}", launching.getId().get(), launching.getState());
    moduleBuildService.begin(launching);
  }


}
