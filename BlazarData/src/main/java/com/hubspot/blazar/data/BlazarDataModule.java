package com.hubspot.blazar.data;

import com.google.inject.AbstractModule;
import com.hubspot.blazar.data.service.*;

public class BlazarDataModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new BlazarDaoModule());

    bind(BuildDefinitionService.class);
    bind(BuildStateService.class);
    bind(BranchService.class);
    bind(ModuleService.class);
    bind(BuildService.class);
    bind(DependenciesService.class);
    bind(EventService.class);
  }
}
