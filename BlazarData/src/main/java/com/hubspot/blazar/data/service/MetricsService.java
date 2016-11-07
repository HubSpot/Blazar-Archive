package com.hubspot.blazar.data.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.metrics.ActiveBranchBuildsInState;
import com.hubspot.blazar.base.metrics.ActiveInterProjectBuildsInState;
import com.hubspot.blazar.base.metrics.ActiveModuleBuildsInState;
import com.hubspot.blazar.data.dao.MetricsDao;
import com.hubspot.blazar.data.dao.QueueItemDao;
import com.hubspot.blazar.data.queue.QueueItem;

public class MetricsService {
  private static final long BUILD_PROBABLY_HUNG_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30);

  private MetricsDao dao;
  private QueueItemDao queueItemDao;

  @Inject
  public MetricsService(MetricsDao dao, QueueItemDao queueItemDao) {
    this.dao = dao;
    this.queueItemDao = queueItemDao;
  }

  /**
   * This method is not expensive per-se but if you want a single-state or will call this method many times in a
   * single second you should use {@Link CachingMetricsService#getCachedActiveModuleBuildCountByState(state)}
   * @return Collection of build states to counts of builds in that state
   */
  public Map<ModuleBuild.State, Integer> countActiveModuleBuildsByState() {
    Set<ActiveModuleBuildsInState> pairs = dao.countActiveModuleBuildsByState();
    ImmutableMap.Builder<ModuleBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for (ActiveModuleBuildsInState pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

  /**
   * This method is not expensive per-se but if you want a single-state or will call this method many times in a
   * single second you should use {@Link CachingMetricsService#getCachedActiveBranchBuildCountByState(state)}
   * @return Collection of build states to counts of builds in that state
   */
  public Map<RepositoryBuild.State, Integer> countActiveBranchBuildsByState() {
    Set<ActiveBranchBuildsInState> pairs = dao.countActiveBranchBuildsByState();
    ImmutableMap.Builder<RepositoryBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for (ActiveBranchBuildsInState pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

  /**
   * This method is not expensive per-se but if you want a single-state or will call this method many times in a
   * single second you should use {@Link CachingMetricsService#getCachedActiveInterProjectBuildCountByState(state)}
   * @return Collection of build states to counts of builds in that state
   */
  public Map<InterProjectBuild.State, Integer> countActiveInterProjectBuildsByState() {
    Set<ActiveInterProjectBuildsInState> pairs = dao.countActiveInterProjectBuildsByState();
    ImmutableMap.Builder<InterProjectBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for(ActiveInterProjectBuildsInState pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }


  /**
   * This method is not expensive per-se but if you want a single-state or will call this method many times in a
   * single second you should use {@Link CachingMetricsService#checkAndUpdateQueuedItemCountMap(state)}
   * @return Collection of events to counts of unprocessed events in that state
   */
  public Map<Class<?>, Integer> countQueuedEventsByType() {
    Map<Class<?>, Integer> countMap = new HashMap<>();
    Set<QueueItem> items = queueItemDao.getItemsReadyToExecute();
    for (QueueItem item : items) {
      if (countMap.containsKey(item.getType())) {
        countMap.put(item.getType(), countMap.get(item.getType()) + 1);
      } else {
        countMap.put(item.getType(), 1);
      }
    }
    return ImmutableMap.copyOf(countMap);
  }

  /**
   * @return A set of strings describing the builds that are probably hung.
   */
  public Set<RepositoryBuild> getHungRepoBuilds() {
    return dao.getBuildsRunningForLongerThan(System.currentTimeMillis(), BUILD_PROBABLY_HUNG_AGE_MILLIS);
  }
}
