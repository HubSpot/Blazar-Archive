package com.hubspot.blazar.listener;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.externalservice.BuildClusterService;

@Singleton
public class QueuedModuleBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(QueuedModuleBuildVisitor.class);
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final BuildClusterService buildClusterService;

  @Inject
  public QueuedModuleBuildVisitor(RepositoryBuildService repositoryBuildService,
                                  ModuleBuildService moduleBuildService,
                                  BuildClusterService buildClusterService) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.buildClusterService = buildClusterService;
  }

  @Override
  protected void visitQueued(ModuleBuild moduleBuild) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(moduleBuild.getRepoBuildId()).get();
    // we eagerly launch the build containers without waiting for upstreams to finish
    launchBuildContainer(moduleBuild);
    if (upstreamsComplete(repositoryBuild, moduleBuild)) {
      setModuleBuildStateToLaunching(moduleBuild);
    } else {
      moduleBuildService.update(moduleBuild.toBuilder().setState(State.WAITING_FOR_UPSTREAM_BUILD).build());
    }
  }

  @Override
  protected void visitWaitingForUpstreamBuild(ModuleBuild moduleBuild) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(moduleBuild.getRepoBuildId()).get();
    if (upstreamsComplete(repositoryBuild, moduleBuild)) {
      setModuleBuildStateToLaunching(moduleBuild);
    }
  }

  private boolean upstreamsComplete(RepositoryBuild repositoryBuild, ModuleBuild build) {
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();
    String buildingStatusLogPrefix = String.format("ModuleBuild %s for Module %s ", build.getId().get(), build.getModuleId());

    if (dependencyGraph.incomingVertices(build.getModuleId()).isEmpty()) {
      LOG.info("{} has no upstreams it is ready to build.", buildingStatusLogPrefix);
      return true;
    } else {
      Set<ModuleBuild> moduleBuilds = moduleBuildService.getByRepositoryBuild(build.getRepoBuildId());
      Set<Integer> buildingModules = extractModuleIds(filterSucceeded(moduleBuilds));
      Set<Integer> upstreamModules = dependencyGraph.getAllUpstreamNodes(build.getModuleId());
      Set<Integer> buildingUpstreams = Sets.intersection(buildingModules, upstreamModules);

      if (buildingUpstreams.isEmpty()) {
        LOG.info("{} is no longer waiting for upstreams and is ready to build", buildingStatusLogPrefix);
        return true;
      }

      Set<Long> runningUpstreamModuleBuildIds = new HashSet<>();
      for (ModuleBuild moduleBuild : moduleBuilds) {
        if (buildingUpstreams.contains(moduleBuild.getModuleId())) {
          runningUpstreamModuleBuildIds.add(build.getId().get());
        }
      }

      LOG.info("{} is waiting for ModuleBuilds: {}", buildingStatusLogPrefix, runningUpstreamModuleBuildIds);
      return false;
    }
  }

  // Here we will eagerly launch the build container in the cluster before we check whether there are upstreams
  // that will need to finish before this build can commence.
  // We start the build containers eagerly since it takes quite long to spin up the docker images and we want
  // to take advantage of the time upstreams may be building to launch the container.
  // So despite launching the containers here we are NOT YET setting the state of the module build
  // to LAUNCHING because this would signal the build container to start building.
  private void launchBuildContainer(ModuleBuild moduleBuild) throws Exception {
    final String buildCluster;
    try {
      buildCluster = buildClusterService.launchBuildContainer(moduleBuild);
      LOG.info("Docker container was successfully launched in cluster {} for module build {}",
          buildCluster, moduleBuild.getId().get());
    } catch (Exception e) {
      throw new NonRetryableBuildException(String.format("An error occurred while launching docker container for module build %s. Will fail the build", moduleBuild), e);
    }
  }

  // when a module is ready to start building we signal the build container to start the build by setting the
  // state of the module build to LAUNCHING
  private void setModuleBuildStateToLaunching(ModuleBuild moduleBuild) throws Exception {
    ModuleBuild launching = moduleBuild.toBuilder()
        .setStartTimestamp(Optional.of(System.currentTimeMillis()))
        .setState(State.LAUNCHING)
        .build();

    moduleBuildService.setToLaunching(launching);
    LOG.info("Updated status of Module Build {} to {}", launching.getId().get(), launching.getState());
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
