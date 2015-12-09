package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;
import com.hubspot.blazar.data.service.ModuleBuildService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class CancelledRepositoryBuildListener implements RepositoryBuildListener {
  private final ModuleBuildService moduleBuildService;

  @Inject
  public CancelledRepositoryBuildListener(ModuleBuildService moduleBuildService) {
    this.moduleBuildService = moduleBuildService;
  }

  @Override
  public void buildChanged(RepositoryBuild repositoryBuild) throws Exception {
    Set<ModuleBuild> builds = moduleBuildService.getByRepositoryBuild(repositoryBuild.getId().get());
    for (ModuleBuild build : builds) {
      if (!build.getState().isComplete()) {
        moduleBuildService.cancel(build);
      }
    }
  }
}
