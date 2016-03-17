package com.hubspot.blazar.listener;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class BuildEventDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildEventDispatcher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final Set<RepositoryBuildVisitor> repositoryVisitors;
  private final Set<ModuleBuildVisitor> moduleVisitors;

  @Inject
  public BuildEventDispatcher(RepositoryBuildService repositoryBuildService,
                              ModuleBuildService moduleBuildService,
                              Set<RepositoryBuildVisitor> repositoryVisitors,
                              Set<ModuleBuildVisitor> moduleVisitors,
                              EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.repositoryVisitors = repositoryVisitors;
    this.moduleVisitors = moduleVisitors;

    eventBus.register(this);
  }

  @Subscribe
  public void dispatch(RepositoryBuild build) throws Exception {
    RepositoryBuild current = repositoryBuildService.get(build.getId().get()).get();
    if (current.getState() != build.getState()) {
      LOG.warn("Ignoring stale event with state {} for repository build {}, current state is {}", build.getState(), build.getId().get(), current.getState());
      return;
    } else {
      build = current;
    }

    try {
      for (RepositoryBuildVisitor visitor : repositoryVisitors) {
        visitor.visit(build);
      }
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", build.getId().get(), e);
      repositoryBuildService.fail(build);
    }
  }

  @Subscribe
  public void dispatch(ModuleBuild build) throws Exception {
    ModuleBuild current = moduleBuildService.get(build.getId().get()).get();
    if (!matchingState(current.getState(), build.getState())) {
      LOG.warn("Ignoring stale event with state {} for module build {}, current state is {}", build.getState(), build.getId().get(), current.getState());
      return;
    } else {
      build = current;
    }

    try {
      for (ModuleBuildVisitor visitor : moduleVisitors) {
        visitor.visit(build);
      }
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", build.getId().get(), e);
      moduleBuildService.fail(build);
    }
  }

  private boolean matchingState(ModuleBuild.State current, ModuleBuild.State other) {
    if (current == other) {
      return true;
    } else if (current.isComplete()) {
      return false;
    } else {
      return current.getSimpleState() == other.getSimpleState();
    }
  }
}
