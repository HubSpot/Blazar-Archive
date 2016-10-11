package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.SqlQuery;

import com.hubspot.blazar.base.metrics.StateToActiveBranchBuildCountPair;
import com.hubspot.blazar.base.metrics.StateToActiveInterProjectBuildCountPair;
import com.hubspot.blazar.base.metrics.StateToActiveModuleBuildCountPair;

public interface MetricsDao {

  @SqlQuery("SELECT state, COUNT(id) AS count FROM module_builds " +
      "WHERE state IN ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<StateToActiveModuleBuildCountPair> countActiveModuleBuildsByState();

  @SqlQuery("SELECT state, COUNT(id) AS count FROM repo_builds " +
      "WHERE state IN ('QUEUED', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<StateToActiveBranchBuildCountPair> countActiveBranchBuildsByState();

  @SqlQuery("SELECT state, count(id) AS count FROM inter_project_builds " +
      "WHERE state in ('QUEUED', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<StateToActiveInterProjectBuildCountPair> countActiveInterProjectBuildsByState();

}
