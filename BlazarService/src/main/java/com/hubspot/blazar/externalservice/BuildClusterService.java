package com.hubspot.blazar.externalservice;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityClusterConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.singularity.api.SingularityRunNowRequest;
import com.hubspot.singularity.client.SingularityClient;

@Singleton
public class BuildClusterService {

  private final Logger LOG = LoggerFactory.getLogger(BuildClusterService.class);

  private final Map<String, SingularityClient> singularityClusterClients;
  private final BlazarConfiguration blazarConfiguration;
  private final ModuleService moduleService;
  private final ModuleBuildService moduleBuildService;
  private final BranchService branchService;
  private final BuildClusterHealthChecker buildClusterHealthChecker;
  private final List<String> availableClusters;
  private final AtomicInteger nextClusterIndex;

  @Inject
  public BuildClusterService(Map<String, SingularityClient> singularityClusterClients,
                             BlazarConfiguration blazarConfiguration,
                             ModuleService moduleService,
                             ModuleBuildService moduleBuildService,
                             BranchService branchService,
                             BuildClusterHealthChecker buildClusterHealthChecker) {
    this.singularityClusterClients = singularityClusterClients;
    this.blazarConfiguration = blazarConfiguration;
    this.moduleService = moduleService;
    this.moduleBuildService = moduleBuildService;
    this.buildClusterHealthChecker = buildClusterHealthChecker;
    this.branchService = branchService;

    availableClusters = ImmutableList.<String>builder().addAll(singularityClusterClients.keySet()).build();
    nextClusterIndex = new AtomicInteger(0);
  }

  public synchronized void launchBuild(ModuleBuild moduleBuild) throws NonRetryableBuildException {
    Optional<String> clusterToUse = pickClusterToLaunchBuild(moduleBuild, Collections.emptySet());
    if (!clusterToUse.isPresent()) {
      throw new NonRetryableBuildException(String.format("Could not find a cluster to build module %d", moduleBuild.getModuleId()));
    }
    SingularityClient singularityClient = singularityClusterClients.get(clusterToUse);
    SingularityClusterConfiguration singularityClusterConfiguration = blazarConfiguration.getSingularityClusterConfigurations().get(clusterToUse);
    singularityClient.runSingularityRequest(singularityClusterConfiguration.getRequest(), Optional.of(buildRequest(moduleBuild)));
  }

  private Optional<String> pickClusterToLaunchBuild(ModuleBuild moduleBuild, Set<String> examinedClusters) throws NonRetryableBuildException {
    if (examinedClusters.equals(availableClusters)) {
      return Optional.absent();
    }

    int clusterIndex = nextClusterIndex.incrementAndGet();
    String clusterToUse = availableClusters.get((clusterIndex % availableClusters.size()) - 1);
    examinedClusters.add(clusterToUse);
    SingularityClusterConfiguration singularityClusterConfiguration = blazarConfiguration.getSingularityClusterConfigurations().get(clusterToUse);

    switch (singularityClusterConfiguration.getBuildStrategy()) {
      case ALWAYS:
        return checkAvailabilityAndPersistCluster(clusterToUse, moduleBuild, examinedClusters);
      case WHITELIST:
        if (singularityClusterConfiguration.getRepositories().isEmpty()) {
          LOG.warn("You have selected the 'WHITELIST' build strategy for cluster {} but you have not provided any repositories. So the cluster is considered always available", clusterToUse);
          return checkAvailabilityAndPersistCluster(clusterToUse, moduleBuild, examinedClusters);
        }
        Optional<String> moduleRepository = getModuleRepository(moduleBuild.getModuleId());
        if (!moduleRepository.isPresent()) {
          throw new NonRetryableBuildException(String.format("Could not get the repository for module %d", moduleBuild.getModuleId()));
        }
        if (singularityClusterConfiguration.getRepositories().contains(moduleRepository.get())) {
          return checkAvailabilityAndPersistCluster(clusterToUse, moduleBuild, examinedClusters);
        }
        return pickClusterToLaunchBuild(moduleBuild, examinedClusters);
      case EXCLUSIVE_WHITELIST:
      case EMERGENCY:
      case EMERGENCY_AND_WHITELIST:
      case EMERGENCY_AND_EXCLUSIVE_WHITELIST:
      default:
        throw new RuntimeException(String.format("Strategy %s is not yet implemented", singularityClusterConfiguration.getBuildStrategy()));
    }
  }

  private SingularityRunNowRequest buildRequest(ModuleBuild moduleBuild) {
    String buildId = Long.toString(moduleBuild.getId().get());
    Optional<com.hubspot.mesos.Resources> buildResources = Optional.absent();
    if (moduleBuild.getResolvedConfig().isPresent() && moduleBuild.getResolvedConfig().get().getBuildResources().isPresent()) {
      buildResources = Optional.of(moduleBuild.getResolvedConfig().get().getBuildResources().get().toMesosResources());
    }

    return new SingularityRunNowRequest(
        Optional.of("Running Blazar module build " + buildId),
        Optional.of(false),
        Optional.of(buildId),
        Optional.of(Arrays.asList("--buildId", buildId)),
        buildResources);
  }

  private Optional<String> getModuleRepository(int moduleId) {
    int branchId = moduleService.getBranchIdFromModuleId(moduleId);
    Optional<GitInfo> moduleBranchMaybe = branchService.get(branchId);
    if (moduleBranchMaybe.isPresent()) {
      GitInfo moduleBranch = moduleBranchMaybe.get();
      return Optional.of(String.format("%s-%s-%s", moduleBranch.getHost(), moduleBranch.getOrganization(), moduleBranch.getRepository()));
    }
    return Optional.absent();
  }

  private Optional<String> checkAvailabilityAndPersistCluster(String clusterToUse,
                                                              ModuleBuild moduleBuild,
                                                              Set<String> examinedClusters) throws NonRetryableBuildException{
    if (buildClusterHealthChecker.isClusterAvailable(clusterToUse)) {
      moduleBuildService.updateBuildClusterName(moduleBuild.getModuleId(), clusterToUse);
      return Optional.of(clusterToUse);
    } else {
      LOG.warn("Build cluster {} is unavailable. Will look for another cluster", clusterToUse);
      return pickClusterToLaunchBuild(moduleBuild, examinedClusters);
    }
  }

}
