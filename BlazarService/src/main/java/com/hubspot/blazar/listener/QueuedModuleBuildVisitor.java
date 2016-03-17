package com.hubspot.blazar.listener;

import com.google.common.collect.Sets;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.ModuleBuildLauncher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class QueuedModuleBuildVisitor extends AbstractModuleBuildVisitor {
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final ModuleBuildLauncher moduleBuildLauncher;

  @Inject
  public QueuedModuleBuildVisitor(RepositoryBuildService repositoryBuildService,
                                  ModuleBuildService moduleBuildService,
                                  ModuleBuildLauncher moduleBuildLauncher) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.moduleBuildLauncher = moduleBuildLauncher;
  }

  @Override
  protected void visitQueued(ModuleBuild build) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    if (upstreamsComplete(repositoryBuild, build)) {
      moduleBuildLauncher.launch(repositoryBuild, build);
    } else {
      moduleBuildService.update(build.withState(State.WAITING_FOR_UPSTREAM_BUILD));
    }
  }

  @Override
  protected void visitWaitingForUpstreamBuild(ModuleBuild build) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    if (upstreamsComplete(repositoryBuild, build)) {
      moduleBuildLauncher.launch(repositoryBuild, build);
    }
  }

  private boolean upstreamsComplete(RepositoryBuild repositoryBuild, ModuleBuild build) {
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    if (dependencyGraph.incomingVertices(build.getModuleId()).isEmpty()) {
      return true;
    } else {
      Set<ModuleBuild> moduleBuilds = moduleBuildService.getByRepositoryBuild(build.getRepoBuildId());
      Set<Integer> buildingModules = extractModuleIds(filterSucceeded(moduleBuilds));
      Set<Integer> upstreamModules = dependencyGraph.incomingVertices(build.getModuleId());

      return Sets.intersection(buildingModules, upstreamModules).isEmpty();
    }
  }

  private static Set<ModuleBuild> filterSucceeded(Set<ModuleBuild> builds) {
    Set<State> allowedStates = EnumSet.complementOf(EnumSet.of(State.SUCCEEDED, State.SKIPPED));

    Set<ModuleBuild> filtered = new HashSet<>();
    for (ModuleBuild build : builds) {
      if (allowedStates.contains(build.getState())) {
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
