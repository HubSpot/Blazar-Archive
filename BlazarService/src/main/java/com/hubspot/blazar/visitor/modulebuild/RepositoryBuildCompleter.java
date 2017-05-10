package com.hubspot.blazar.visitor.modulebuild;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

@Singleton
public class RepositoryBuildCompleter implements ModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryBuildCompleter.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;

  @Inject
  public RepositoryBuildCompleter(RepositoryBuildService repositoryBuildService,
                                  ModuleBuildService moduleBuildService) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
  }

  @Override
  public void visit(ModuleBuild build) throws Exception {
    if (!build.getState().isComplete()) {
      // repository build can't be done if this module isn't done
      return;
    }

    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    if (repositoryBuild.getState().isComplete()) {
      // already done, this must be a stale ModuleBuild event
      return;
    }

    Set<ModuleBuild> builds = moduleBuildService.getByRepositoryBuild(build.getRepoBuildId());
    if (allComplete(builds)) {
      LOG.info("All module builds complete, going to complete repository build {}", build.getRepoBuildId());
      RepositoryBuild.State finalState = determineRepositoryBuildState(builds);
      LOG.info("Final state for repository build {} is {}", build.getRepoBuildId(), finalState);
      RepositoryBuild completed = repositoryBuild.toBuilder()
          .setState(finalState)
          .setEndTimestamp(Optional.of(System.currentTimeMillis()))
          .build();
      repositoryBuildService.update(completed);
    }
  }

  private RepositoryBuild.State determineRepositoryBuildState(Set<ModuleBuild> builds) {
    for (ModuleBuild build : builds) {
      if (build.getState() == ModuleBuild.State.FAILED) {
        return RepositoryBuild.State.FAILED;
      }
    }

    for (ModuleBuild build : builds) {
      if (build.getState() == ModuleBuild.State.CANCELLED) {
        return RepositoryBuild.State.CANCELLED;
      }
    }

    // if a module was skipped we need to carry over any previous failure
    for (ModuleBuild build : builds) {
      if (build.getState() == ModuleBuild.State.SKIPPED) {
        if (!lastBuildSucceeded(build)) {
          return RepositoryBuild.State.UNSTABLE;
        }
      }
    }

    return RepositoryBuild.State.SUCCEEDED;
  }

  private boolean lastBuildSucceeded(ModuleBuild build) {
    Optional<ModuleBuild> previous = moduleBuildService.getPreviousBuild(build);
    return previous.isPresent() && previous.get().getState() == ModuleBuild.State.SUCCEEDED;
  }

  private static boolean allComplete(Set<ModuleBuild> builds) {
    for (ModuleBuild build : builds) {
      if (!build.getState().isComplete()) {
        return false;
      }
    }

    return true;
  }
}
