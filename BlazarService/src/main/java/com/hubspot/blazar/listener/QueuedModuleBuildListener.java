package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.ModuleBuildLauncher;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QueuedModuleBuildListener implements ModuleBuildListener {
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildLauncher moduleBuildLauncher;

  @Inject
  public QueuedModuleBuildListener(RepositoryBuildService repositoryBuildService,
                                   ModuleBuildLauncher moduleBuildLauncher) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildLauncher = moduleBuildLauncher;
  }

  @Override
  public void buildChanged(ModuleBuild build) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    if (dependencyGraph.incomingVertices(build.getModuleId()).isEmpty()) {
      moduleBuildLauncher.launch(repositoryBuild, build);
    }
  }
}
