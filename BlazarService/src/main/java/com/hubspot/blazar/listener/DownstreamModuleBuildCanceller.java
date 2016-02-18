package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class DownstreamModuleBuildCanceller extends AbstractModuleBuildVisitor {
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;

  @Inject
  public DownstreamModuleBuildCanceller(RepositoryBuildService repositoryBuildService,
                                        ModuleBuildService moduleBuildService) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
  }

  @Override
  protected void visitCancelled(ModuleBuild build) throws Exception {
    cancelDownstreamModuleBuilds(build);
  }

  @Override
  protected void visitFailed(ModuleBuild build) throws Exception {
    cancelDownstreamModuleBuilds(build);
  }

  private void cancelDownstreamModuleBuilds(ModuleBuild build) {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    Set<Integer> downstreamModules = dependencyGraph.outgoingVertices(build.getModuleId());
    for (ModuleBuild otherBuild : moduleBuildService.getByRepositoryBuild(build.getRepoBuildId())) {
      if (downstreamModules.contains(otherBuild.getModuleId()) && !otherBuild.getState().isComplete()) {
        moduleBuildService.cancel(otherBuild);
      }
    }
  }
}
