package com.hubspot.blazar.util;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.exception.BuildClusterException;
import com.hubspot.blazar.externalservice.BuildClusterHealthChecker;
import com.hubspot.blazar.externalservice.BuildClusterService;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.singularity.client.SingularityClient;

@Singleton
public class TestBuildClusterService extends BuildClusterService {
  private static final Logger LOG = LoggerFactory.getLogger(TestBuildClusterService.class);
  private final ModuleBuildResource moduleBuildResource;
  private Set<Integer> failingModules;

  @Inject
  public TestBuildClusterService(Map<String, SingularityClient> singularityClusterClients,
                                 BlazarConfiguration blazarConfiguration,
                                 ModuleService moduleService,
                                 ModuleBuildService moduleBuildService,
                                 BranchService branchService,
                                 BuildClusterHealthChecker buildClusterHealthChecker,
                                 ModuleBuildResource moduleBuildResource,
                                 AsyncHttpClient asyncHttpClient) {
    super(singularityClusterClients, blazarConfiguration, moduleService, moduleBuildService, branchService, buildClusterHealthChecker, asyncHttpClient);
    this.moduleBuildResource = moduleBuildResource;
    this.failingModules = ImmutableSet.of();
  }


  public void clearModulesToFail() {
    failingModules = ImmutableSet.of();
  }

  public void setModulesToFail(Set<Integer> modules) {
    failingModules = ImmutableSet.copyOf(modules);
  }

  @Override
  public synchronized void launchBuildContainer(ModuleBuild build) throws BuildClusterException {
    build = moduleBuildService.get(build.getId().get()).get();
    if (build.getState().isWaiting()) {
      return;
    }
    if (failingModules.contains(build.getModuleId())) {
      failBuild(build);
    } else {
      passBuild(build);
    }
  }

  private void failBuild(ModuleBuild build) {
    LOG.info("Pretending to launch {} calling start", build);
    ModuleBuild inProgress = moduleBuildResource.start(build.getId().get(), Optional.of(build.toString()));
    LOG.info("Build {} now in progress, publishing failure", inProgress);
    ModuleBuild failure = moduleBuildResource.completeFailure(inProgress.getId().get());
    LOG.info("Build {} Failed", failure);
  }

  private void passBuild(ModuleBuild build) {
    LOG.info("Pretending to launch {} calling start", build);
    ModuleBuild inProgress = moduleBuildResource.start(build.getId().get(), Optional.of(build.toString()));
    LOG.info("Build {} now in progress, publishing success", inProgress);
    ModuleBuild success = moduleBuildResource.completeSuccess(inProgress.getId().get());
    LOG.info("Build {} succeed", success);
  }

}
