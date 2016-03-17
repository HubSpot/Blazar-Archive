package com.hubspot.blazar.data;

import com.google.inject.AbstractModule;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectModuleBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.InterProjectRepositoryBuildMappingService;
import com.hubspot.blazar.data.service.MalformedFileService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.service.InstantMessageConfigurationService;
import com.hubspot.blazar.data.service.StateService;

public class BlazarDataModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new BlazarDaoModule());

    bind(BranchService.class);
    bind(ModuleService.class);
    bind(StateService.class);
    bind(RepositoryBuildService.class);
    bind(ModuleBuildService.class);
    bind(DependenciesService.class);
    bind(MalformedFileService.class);
    bind(ModuleDiscoveryService.class);
    bind(InstantMessageConfigurationService.class);
    bind(InterProjectBuildService.class);
    bind(InterProjectModuleBuildMappingService.class);
    bind(InterProjectRepositoryBuildMappingService.class);
  }
}
