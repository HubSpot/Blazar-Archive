package com.hubspot.blazar.util;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.DependenciesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

public class DependencyBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(DependencyBuilder.class);

  private final DependenciesService dependenciesService;
  private final BuildDefinitionService buildDefinitionService;

  @Inject
  public DependencyBuilder(DependenciesService dependenciesService,
                           BuildDefinitionService buildDefinitionService,
                           EventBus eventBus) {
    this.dependenciesService = dependenciesService;
    this.buildDefinitionService = buildDefinitionService;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildStateChange(Build build) throws IOException {
    if (build.getState() == State.SUCCEEDED) {
      BuildDefinition definition = buildDefinitionService.getByModule(build.getModuleId()).get();
      GitInfo gitInfo = definition.getGitInfo();

      Set<ModuleDependency> branchProvides = dependenciesService.getProvides(gitInfo);
      Set<ModuleDependency> branchDepends = dependenciesService.getDepends(gitInfo);


      LOG.info("Going to build dependency graph and kick off some builds!");
    }
  }
}
