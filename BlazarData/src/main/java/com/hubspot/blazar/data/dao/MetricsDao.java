package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import com.hubspot.blazar.base.RepositoryBuild;
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

  @SqlQuery("SELECT * FROM repo_builds WHERE state in ('LAUNCHING', 'IN_PROGRESS') AND endTimestamp IS NULL AND :currentTimeMillis - startTimestamp > :maxAgeMillis ")
  Set<RepositoryBuild> getBuildsRunningForLongerThan(@Bind("currentTimeMillis") long currentTimeMillis, @Bind("maxAgeMillis") long maxAgeMillis);

}
