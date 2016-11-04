package com.hubspot.blazar.data.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;

public class CachingMetricsService {
  private static final Logger LOG = LoggerFactory.getLogger(CachingMetricsService.class);
  private static final long MODULE_BUILD_COUNT_MAX_AGE_MILLIS = 500;
  private static final long BRANCH_BUILD_COUNT_MAX_AGE_MILLIS = 500;
  private static final long INTER_PROJECT_BUILD_COUNT_MAX_AGE_MILLIS = 500;
  private static final long QUEUED_ITEM_COUNT_MAX_AGE_MILLIS = 500;
  private static final long HUNG_BUILD_SET_MAX_AGE_MILLIS = 500;

  private final MetricsService metricsService;

  private volatile Map<ModuleBuild.State, Integer> moduleBuildCountMap = ImmutableMap.of();
  private volatile Map<RepositoryBuild.State, Integer> repoBuildCountMap = ImmutableMap.of();
  private volatile Map<InterProjectBuild.State, Integer> interProjectCountMap = ImmutableMap.of();
  private volatile Map<Class<?>, Integer> queuedItemCountMap = ImmutableMap.of();
  private volatile Set<RepositoryBuild> hungRepositoryBuilds = ImmutableSet.of();

  private volatile long moduleBuildCountMapLastWrite = 0;
  private volatile long repoBuildCountMapLastWrite = 0;
  private volatile long interProjectBuildCountMapLastWrite = 0;
  private volatile long queuedItemCountMapLastWrite = 0;
  private volatile long hungRepositoryBuildsLastWrite = 0;


  @Inject
  public CachingMetricsService(MetricsService metricsService) {
    this.metricsService = metricsService;
  }

  public synchronized int getCachedActiveModuleBuildCountByState(ModuleBuild.State state) {
    if (isTooOld(moduleBuildCountMapLastWrite, MODULE_BUILD_COUNT_MAX_AGE_MILLIS)) {
      LOG.info("Refreshing moduleBuildCountMap cache");
      moduleBuildCountMap = metricsService.countActiveModuleBuildsByState();
      moduleBuildCountMapLastWrite = System.currentTimeMillis();
    }

    if (!moduleBuildCountMap.containsKey(state)) {
      LOG.info("No such state {} in count results returning 0", state);
      return 0;
    }

    return moduleBuildCountMap.get(state);
  }

  public synchronized int getCachedActiveBranchBuildCountByState(RepositoryBuild.State state) {
    if (isTooOld(repoBuildCountMapLastWrite, BRANCH_BUILD_COUNT_MAX_AGE_MILLIS)) {
      LOG.info("Refreshing branchBuildCountMap cache");
      repoBuildCountMap = metricsService.countActiveBranchBuildsByState();
      repoBuildCountMapLastWrite = System.currentTimeMillis();
    }

    if (!repoBuildCountMap.containsKey(state)) {
      LOG.info("No such state {} in count results returning 0", state);
      return 0;
    }

    return repoBuildCountMap.get(state);
  }

  public synchronized int getCachedActiveInterProjectBuildCountByState(InterProjectBuild.State state) {
    if (isTooOld(interProjectBuildCountMapLastWrite, INTER_PROJECT_BUILD_COUNT_MAX_AGE_MILLIS)) {
      LOG.info("Refreshing interProjectBuildCountMap cache");
      interProjectCountMap = metricsService.countActiveInterProjectBuildsByState();
      interProjectBuildCountMapLastWrite = System.currentTimeMillis();
    }

    if (!interProjectCountMap.containsKey(state)) {
      LOG.info("No such state {} in count results returning 0", state);
      return 0;
    }

    return interProjectCountMap.get(state);
  }

  public int getCachedQueuedItemCountByType(Class<?> type) {
    checkAndUpdateQueuedItemCountMap();

    if (!queuedItemCountMap.containsKey(type)) {
      LOG.info("No events of type {} in count results returning 0", type);
      return 0;
    }

    return queuedItemCountMap.get(type);
  }

  public int getCachedHungBuildCount() {
    checkAndUpdateHungBuildsSet();
    return hungRepositoryBuilds.size();
  }

  public String getCachedHungBuildData() {
    checkAndUpdateHungBuildsSet();
    Set<String> hungBuildData = new HashSet<>();
    long currentTime = System.currentTimeMillis();
    for (RepositoryBuild build : hungRepositoryBuilds) {
      hungBuildData.add(String.format("Branch %d BuildNumber %d BuildId %d Age %d",
          build.getBranchId(),
          build.getBuildNumber(),
          build.getId().get(),
          currentTime - build.getStartTimestamp().get()));
    }
    return Joiner.on("\n").join(hungBuildData);
  }

  private synchronized void checkAndUpdateHungBuildsSet() {
    if (isTooOld(hungRepositoryBuildsLastWrite, HUNG_BUILD_SET_MAX_AGE_MILLIS)) {
      LOG.info("Refreshing hungRepositoryBuilds cache");
      hungRepositoryBuilds = metricsService.getHungRepoBuilds();
      hungRepositoryBuildsLastWrite = System.currentTimeMillis();
    }
  }

  public int getPendingEventTypeCount() {
    checkAndUpdateQueuedItemCountMap();
    return queuedItemCountMap.keySet().size();
  }

  private synchronized void checkAndUpdateQueuedItemCountMap() {
    if (isTooOld(queuedItemCountMapLastWrite, QUEUED_ITEM_COUNT_MAX_AGE_MILLIS)) {
      LOG.info("Refreshing queuedItemCountMap cache");
      queuedItemCountMap = metricsService.countQueuedEventsByType();
      queuedItemCountMapLastWrite = System.currentTimeMillis();
    }
  }

  private boolean isTooOld(long lastWrite, long maxAge) {
    return System.currentTimeMillis() - lastWrite > maxAge;
  }
}
