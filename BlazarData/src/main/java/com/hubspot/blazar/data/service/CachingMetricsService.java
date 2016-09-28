package com.hubspot.blazar.data.service;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;

@Singleton
public class CachingMetricsService {
  private static final long MODULE_BUILD_COUNT_MAX_AGE_MILLIS = 500;
  private static final long BRANCH_BUILD_COUNT_MAX_AGE_MILLIS = 500;
  private final MetricsService metricsService;
  private Map<ModuleBuild.State, Integer> moduleBuildCountMap;
  private Map<RepositoryBuild.State, Integer> repoBuildCountMap;
  private long moduleBuildCountMapLastWrite;
  private long repoBuildCountMapLastWrite;

  @Inject
  public CachingMetricsService(MetricsService metricsService) {
    this.metricsService = metricsService;
    this.moduleBuildCountMap = new HashMap<>();
    this.moduleBuildCountMapLastWrite = 0;
    this.repoBuildCountMapLastWrite = 0;
  }

  public Integer getCachedActiveModuleBuildCountByState(ModuleBuild.State state) {
    long currentTime = System.currentTimeMillis();
    if (currentTime - moduleBuildCountMapLastWrite > MODULE_BUILD_COUNT_MAX_AGE_MILLIS) {
      // refresh cache
      moduleBuildCountMap = metricsService.countActiveModuleBuildsByState();
    }

    if (!moduleBuildCountMap.containsKey(state)) {
      return 0;
    }
    return moduleBuildCountMap.get(state);
  }

  public Integer getCachedActiveBranchBuildCountByState(RepositoryBuild.State state) {
    long currentTime = System.currentTimeMillis();
    if (currentTime - repoBuildCountMapLastWrite > BRANCH_BUILD_COUNT_MAX_AGE_MILLIS) {
      repoBuildCountMap = metricsService.countActiveBranchBuildsByState();
    }

    if (!repoBuildCountMap.containsKey(state)) {
      return 0;
    }

    return repoBuildCountMap.get(state);
  }
}
