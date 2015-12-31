package com.hubspot.blazar.listener;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

@Singleton
public class BuildEventDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildEventDispatcher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final Map<RepositoryBuild.State, Set<RepositoryBuildListener>> repositoryListeners;
  private final Map<ModuleBuild.State, Set<ModuleBuildListener>> moduleListeners;

  @Inject
  public BuildEventDispatcher(RepositoryBuildService repositoryBuildService,
                              ModuleBuildService moduleBuildService,
                              Map<RepositoryBuild.State, Set<RepositoryBuildListener>> repositoryListeners,
                              Map<ModuleBuild.State, Set<ModuleBuildListener>> moduleListeners,
                              EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.repositoryListeners = repositoryListeners;
    this.moduleListeners = moduleListeners;

    eventBus.register(this);
  }

  @Subscribe
  public void dispatch(RepositoryBuild build) throws Exception {
    LOG.info("L45: {}", build.getBuildTrigger());
    RepositoryBuild current = repositoryBuildService.get(build.getId().get()).get();
    LOG.info("L47: {}", current.getBuildTrigger());
    if (current.getState() != build.getState()) {
      LOG.warn("Ignoring stale event with state {} for repository build {}, current state is {}", build.getState(), build.getId().get(), current.getState());
      return;
    } else {
      build = current;
    }

    try {
      if (repositoryListeners.containsKey(build.getState())) {
        for (RepositoryBuildListener listener : repositoryListeners.get(build.getState())) {
          listener.buildChanged(build);
        }
      }
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", build.getId().get(), e);
      repositoryBuildService.fail(build);
    }
  }

  @Subscribe
  public void dispatch(ModuleBuild build) throws Exception {
    ModuleBuild current = moduleBuildService.get(build.getId().get()).get();
    if (current.getState() != build.getState()) {
      LOG.warn("Ignoring stale event with state {} for module build {}, current state is {}", build.getState(), build.getId().get(), current.getState());
      return;
    } else {
      build = current;
    }

    try {
      if (moduleListeners.containsKey(build.getState())) {
        for (ModuleBuildListener listener : moduleListeners.get(build.getState())) {
          listener.buildChanged(build);
        }
      }
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", build.getId().get(), e);
      moduleBuildService.fail(build);
    }
  }
}
