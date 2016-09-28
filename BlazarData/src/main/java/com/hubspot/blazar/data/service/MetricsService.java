package com.hubspot.blazar.data.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.metrics.StateToActiveBuildCountPair;
import com.hubspot.blazar.data.dao.MetricsDao;

public class MetricsService {

  private MetricsDao dao;

  @Inject
  public MetricsService(MetricsDao dao) {
    this.dao = dao;
  }

  public Map<ModuleBuild.State, Integer> countActiveModuleBuildsByState() {
    Set<StateToActiveBuildCountPair> pairs = dao.countActiveModuleBuildsByState();
    Map<ModuleBuild.State, Integer> stateCountMap = new HashMap<>();
    for (StateToActiveBuildCountPair pair : pairs) {
      stateCountMap.put(ModuleBuild.State.valueOf(pair.getStateName()), pair.getCount());
    }
    return stateCountMap;
  }

  public Map<RepositoryBuild.State, Integer> countActiveBranchBuildsByState() {
    Set<StateToActiveBuildCountPair> pairs = dao.countActiveBranchBuildsByState();
    Map<RepositoryBuild.State, Integer> stateCountMap = new HashMap<>();
    for (StateToActiveBuildCountPair pair : pairs) {
      stateCountMap.put(RepositoryBuild.State.valueOf(pair.getStateName()), pair.getCount());
    }
    return stateCountMap;
  }
}
