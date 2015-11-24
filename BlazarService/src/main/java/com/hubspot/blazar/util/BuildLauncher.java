package com.hubspot.blazar.util;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Optional;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.BuildStateService;

@Singleton
public class BuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final BuildStateService buildStateService;
  private final BuildService buildService;

  @Inject
  public BuildLauncher(BuildStateService buildStateService,
                       BuildService buildService,
                       EventBus eventBus) {
    this.buildStateService = buildStateService;
    this.buildService = buildService;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(Build build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    final BuildDefinition buildDefinition;
    final Build buildToLaunch;
    final Optional<Build> previousBuild;
    if (build.getState() == State.QUEUED) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId()).get();
      Optional<Build> pendingBuild = buildState.getPendingBuild();
      if (buildState.getInProgressBuild().isPresent()) {
        LOG.info("In progress build for module {}, not launching build {}", build.getModuleId(), build.getId().get());
        return;
      } else if (!pendingBuild.isPresent() || !build.getId().equals(pendingBuild.get().getId())) {
        LOG.info("Build {} is no longer pending for module {}, not launching", build.getId().get(), build.getModuleId());
        return;
      } else {
        LOG.info("No in progress build for module {}, going to launch build {}", build.getModuleId(), build.getId().get());
        buildDefinition = buildState;
        buildToLaunch = pendingBuild.get();
        previousBuild = buildState.getLastBuild();
      }
    } else if (build.getState().isComplete()) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId()).get();
      if (buildState.getPendingBuild().isPresent()) {
        LOG.info("Pending build for module {}, going to launch build {}", build.getModuleId(), buildState.getPendingBuild().get().getId().get());
        buildDefinition = buildState;
        buildToLaunch = buildState.getPendingBuild().get();
        previousBuild = buildState.getLastBuild();
      } else {
        LOG.info("No pending build for module {}", build.getModuleId());
        return;
      }
    } else {
      return;
    }

    try {
      startBuild(buildDefinition, buildToLaunch, previousBuild);
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", buildToLaunch.getId().get(), e);
      buildService.fail(buildToLaunch);
    }
  }
}
