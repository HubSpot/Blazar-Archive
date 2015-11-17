package com.hubspot.blazar.data;

import com.google.inject.AbstractModule;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

public class BlazarDataModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new BlazarDaoModule());

    bind(BuildDefinitionService.class);
    bind(BuildStateService.class);
    bind(BranchService.class);
    bind(ModuleService.class);
    bind(BuildService.class);
    bind(RepositoryBuildService.class);
    bind(ModuleBuildService.class);
    bind(DependenciesService.class);
  }
}
