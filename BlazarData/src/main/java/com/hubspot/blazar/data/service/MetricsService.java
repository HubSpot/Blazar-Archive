package com.hubspot.blazar.data.service;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.metrics.StateToActiveBranchBuildCountPair;
import com.hubspot.blazar.base.metrics.StateToActiveInterProjectBuildCountPair;
import com.hubspot.blazar.base.metrics.StateToActiveModuleBuildCountPair;
import com.hubspot.blazar.data.dao.MetricsDao;

public class MetricsService {

  private MetricsDao dao;

  @Inject
  public MetricsService(MetricsDao dao) {
    this.dao = dao;
  }

  public Map<ModuleBuild.State, Integer> countActiveModuleBuildsByState() {
    Set<StateToActiveModuleBuildCountPair> pairs = dao.countActiveModuleBuildsByState();
    ImmutableMap.Builder<ModuleBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for (StateToActiveModuleBuildCountPair pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

  public Map<RepositoryBuild.State, Integer> countActiveBranchBuildsByState() {
    Set<StateToActiveBranchBuildCountPair> pairs = dao.countActiveBranchBuildsByState();
    ImmutableMap.Builder<RepositoryBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for (StateToActiveBranchBuildCountPair pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }

  public Map<InterProjectBuild.State, Integer> countActiveInterProjectBuildsByState() {
    Set<StateToActiveInterProjectBuildCountPair> pairs = dao.countActiveInterProjectBuildsByState();
    ImmutableMap.Builder<InterProjectBuild.State, Integer> mapBuilder = ImmutableMap.builder();
    for(StateToActiveInterProjectBuildCountPair pair : pairs) {
      mapBuilder.put(pair.getState(), pair.getCount());
    }
    return mapBuilder.build();
  }
}
