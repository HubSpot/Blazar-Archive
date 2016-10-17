package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.SqlQuery;

import com.hubspot.blazar.base.metrics.ActiveBranchBuildsInState;
import com.hubspot.blazar.base.metrics.ActiveInterProjectBuildsInState;
import com.hubspot.blazar.base.metrics.ActiveModuleBuildsInState;

public interface MetricsDao {

  @SqlQuery("SELECT state, COUNT(id) AS count FROM module_builds " +
      "WHERE state IN ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<ActiveModuleBuildsInState> countActiveModuleBuildsByState();

  @SqlQuery("SELECT state, COUNT(id) AS count FROM repo_builds " +
      "WHERE state IN ('QUEUED', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<ActiveBranchBuildsInState> countActiveBranchBuildsByState();

  @SqlQuery("SELECT state, count(id) AS count FROM inter_project_builds " +
      "WHERE state in ('QUEUED', 'LAUNCHING', 'IN_PROGRESS') GROUP BY state")
  Set<ActiveInterProjectBuildsInState> countActiveInterProjectBuildsByState();

}
