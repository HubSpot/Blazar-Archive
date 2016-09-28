package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.SqlQuery;

import com.hubspot.blazar.base.metrics.StateToActiveBuildCountPair;

public interface MetricsDao {

  @SqlQuery("SELECT state AS stateName, COUNT(id) AS count FROM module_builds " +
      "WHERE state IN ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<StateToActiveBuildCountPair> countActiveModuleBuildsByState();

  @SqlQuery("SELECT state AS stateName, COUNT(id) AS count FROM repo_builds " +
      "WHERE state IN ('QUEUED', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<StateToActiveBuildCountPair> countActiveBranchBuildsByState();

}
