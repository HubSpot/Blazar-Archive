package com.hubspot.blazar.listener;

import com.google.common.primitives.Ints;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
      RepositoryBuild.State finalState = determineRepositoryBuildState(repositoryBuild, builds);
      LOG.info("Final state for repository build {} is {}", build.getRepoBuildId(), finalState);
      RepositoryBuild completed = repositoryBuild
          .withState(finalState)
          .withEndTimestamp(System.currentTimeMillis());
      repositoryBuildService.update(completed);
    }
  }

  private RepositoryBuild.State determineRepositoryBuildState(RepositoryBuild repositoryBuild, Set<ModuleBuild> builds) {
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
        if (!lastBuildSucceeded(build.getModuleId())) {
          return RepositoryBuild.State.UNSTABLE;
        }
      }
    }

    return RepositoryBuild.State.SUCCEEDED;
  }

  /**
   * Go back until we find the last build that succeeded or failed for this module
   * (ignore skipped and cancelled builds)
   */
  private boolean lastBuildSucceeded(int moduleId) {
    List<ModuleBuild> builds = new ArrayList<>(moduleBuildService.getByModule(moduleId));
    Collections.sort(builds, new Comparator<ModuleBuild>() {

      @Override
      public int compare(ModuleBuild build1, ModuleBuild build2) {
        return -1 * Ints.compare(build1.getBuildNumber(), build2.getBuildNumber());
      }
    });

    for (ModuleBuild build : builds) {
      if (build.getState() == ModuleBuild.State.SUCCEEDED) {
        return true;
      } else if (build.getState() == ModuleBuild.State.FAILED) {
        return false;
      }
    }

    return false;
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
