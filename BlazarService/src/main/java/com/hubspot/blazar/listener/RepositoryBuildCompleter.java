package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class RepositoryBuildCompleter implements ModuleBuildListener {
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
  public void buildChanged(ModuleBuild build) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();

    // if no downstream builds, we could be done building
    if (noDownstreamBuilds(build, repositoryBuild.getDependencyGraph().get())) {
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

    // TODO: unstable
    return RepositoryBuild.State.SUCCEEDED;
  }

  private static boolean allComplete(Set<ModuleBuild> builds) {
    for (ModuleBuild build : builds) {
      if (!build.getState().isComplete()) {
        return false;
      }
    }

    return true;
  }

  private static boolean noDownstreamBuilds(ModuleBuild build, DependencyGraph dependencyGraph) {
    return dependencyGraph.outgoingVertices(build.getModuleId()).isEmpty();
  }
}
