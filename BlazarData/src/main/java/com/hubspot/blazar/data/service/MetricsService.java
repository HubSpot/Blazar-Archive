package com.hubspot.blazar.data.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

  private MetricsDao dao;
  private QueueItemDao queueItemDao;

  @Inject
  public MetricsService(MetricsDao dao, QueueItemDao queueItemDao) {
    this.dao = dao;
    this.queueItemDao = queueItemDao;
  }

  public Map<ModuleBuild.State, Integer> countActiveModuleBuildsByState() {
    Set<ActiveModuleBuildsInState> pairs = dao.countActiveModuleBuildsByState();
    ImmutableMap.Builder<ModuleBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for (ActiveModuleBuildsInState pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

  public Map<RepositoryBuild.State, Integer> countActiveBranchBuildsByState() {
    Set<ActiveBranchBuildsInState> pairs = dao.countActiveBranchBuildsByState();
    ImmutableMap.Builder<RepositoryBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for (ActiveBranchBuildsInState pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

  public Map<InterProjectBuild.State, Integer> countActiveInterProjectBuildsByState() {
    Set<ActiveInterProjectBuildsInState> pairs = dao.countActiveInterProjectBuildsByState();
    ImmutableMap.Builder<InterProjectBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for(ActiveInterProjectBuildsInState pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

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
}
