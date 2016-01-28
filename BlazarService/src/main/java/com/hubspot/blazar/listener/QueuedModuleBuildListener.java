package com.hubspot.blazar.listener;

import com.google.common.collect.Sets;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.ModuleBuildLauncher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class QueuedModuleBuildListener implements ModuleBuildListener {
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final ModuleBuildLauncher moduleBuildLauncher;

  @Inject
  public QueuedModuleBuildListener(RepositoryBuildService repositoryBuildService,
                                   ModuleBuildService moduleBuildService,
                                   ModuleBuildLauncher moduleBuildLauncher) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.moduleBuildLauncher = moduleBuildLauncher;
  }

  @Override
  public void buildChanged(ModuleBuild build) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();


    if (dependencyGraph.incomingVertices(build.getModuleId()).isEmpty()) {
      moduleBuildLauncher.launch(repositoryBuild, build);
    } else {
      Set<ModuleBuild> moduleBuilds = moduleBuildService.getByRepositoryBuild(build.getRepoBuildId());
      Set<Integer> buildingModules = extractModuleIds(filterSkipped(moduleBuilds));
      Set<Integer> upstreamModules = dependencyGraph.incomingVertices(build.getModuleId());

      if (Sets.intersection(buildingModules, upstreamModules).isEmpty()) {
        moduleBuildLauncher.launch(repositoryBuild, build);
      }
    }
  }

  private static Set<ModuleBuild> filterSkipped(Set<ModuleBuild> builds) {
    Set<ModuleBuild> filtered = new HashSet<>();
    for (ModuleBuild build : builds) {
      if (build.getState() != State.SKIPPED) {
        filtered.add(build);
      }
    }

    return filtered;
  }

  private static Set<Integer> extractModuleIds(Set<ModuleBuild> builds) {
    Set<Integer> moduleIds = new HashSet<>();
    for (ModuleBuild build : builds) {
      moduleIds.add(build.getModuleId());
    }

    return moduleIds;
  }
}
