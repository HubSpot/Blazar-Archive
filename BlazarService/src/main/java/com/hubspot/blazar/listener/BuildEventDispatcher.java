package com.hubspot.blazar.listener;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.InterProjectBuildVisitor;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;

@Singleton
public class BuildEventDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildEventDispatcher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final Set<RepositoryBuildVisitor> repositoryVisitors;
  private final InterProjectBuildService interProjectBuildService;
  private final Set<InterProjectBuildVisitor> interProjectBuildVisitors;
  private final Set<ModuleBuildVisitor> moduleVisitors;

  @Inject
  public BuildEventDispatcher(RepositoryBuildService repositoryBuildService,
                              ModuleBuildService moduleBuildService,
                              InterProjectBuildService interProjectBuildService,
                              Set<RepositoryBuildVisitor> repositoryVisitors,
                              Set<InterProjectBuildVisitor> interProjectBuildVisitors,
                              Set<ModuleBuildVisitor> moduleVisitors,
                              EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.interProjectBuildService = interProjectBuildService;
    this.repositoryVisitors = repositoryVisitors;
    this.interProjectBuildVisitors = interProjectBuildVisitors;
    this.moduleVisitors = moduleVisitors;

    eventBus.register(this);
  }

  @Subscribe
  public void dispatch(RepositoryBuild build) throws Exception {
    Optional<RepositoryBuild> current = repositoryBuildService.get(build.getId().get());
    if (!current.isPresent()) {
      LOG.warn("No repository build {}, ignoring event", build.getId().get());
      return;
    } else if (current.get().getState() != build.getState()) {
      LOG.warn("Ignoring stale event with state {} for repository build {}, current state is {}", build.getState(), build.getId().get(), current.get().getState());
      return;
    } else {
      build = current.get();
    }

    try {
      for (RepositoryBuildVisitor visitor : repositoryVisitors) {
        try {

          visitor.visit(build);
        } catch (Exception e) {
          e.printStackTrace();
          LOG.info("Bad happened");
        }
      }
    } finally {
    }
    //} catch (NonRetryableBuildException e) {
    //  LOG.warn("Failing build {}", build.getId().get(), e);
    //  repositoryBuildService.fail(build);J
    //}
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
        try {
          visitor.visit(build);
        } catch (Exception e) {
          e.printStackTrace();
          LOG.info("Bad happened");
        }
      }
    } finally {
    }
    // } catch (NonRetryableBuildException e) {
    //   LOG.warn("Failing build {}", build.getId().get(), e);
    //   moduleBuildService.fail(build);
    // }
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

  @Subscribe
  public void dispatch(InterProjectBuild build) throws Exception {
    InterProjectBuild current = interProjectBuildService.getWithId(build.getId().get()).get();
    if (current.getState() != build.getState()) {
      LOG.warn("Ignoring stale event with state {} for InterProjectBuild {}, current state is {}", build.getState(), build.getId().get(), current.getState());
      return;
    } else {
      build = current;
    }

    try {
      for (InterProjectBuildVisitor visitor : interProjectBuildVisitors) {
        visitor.visit(build);
      }
    } catch (NonRetryableBuildException e) {
      LOG.warn("Got non Retryable Exception in InterProjectBuild {}, marking as Finished", build.getId().get());
      interProjectBuildService.finish(build);
    }
  }
}
