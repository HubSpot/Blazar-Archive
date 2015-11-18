package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

public interface RepositoryBuildDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM repo_builds_v2 WHERE build.id = :id")
  Optional<RepositoryBuild> get(@Bind("id") long id);

  @SqlQuery("" +
      "SELECT pendingBuild.id AS pendingBuildId, " +
      "pendingBuild.buildNumber AS pendingBuildNumber, " +
      "inProgressBuild.id AS inProgressBuildId, " +
      "inProgressBuild.buildNumber AS inProgressBuildNumber, " +
      "lastBuild.id AS lastBuildId, " +
      "lastBuild.buildNumber AS lastBuildNumber " +
      "FROM branches_v2 b " +
      "LEFT OUTER JOIN repo_builds_v2 AS pendingBuild ON (b.pendingBuildId = pendingBuild.id) " +
      "LEFT OUTER JOIN repo_builds_v2 AS inProgressBuild ON (b.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN repo_builds_v2 AS lastBuild ON (b.lastBuildId = lastBuild.id) " +
      "WHERE b.id = :branchId")
  BuildNumbers getBuildNumbers(@Bind("branchId") int branchId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO repo_builds_v2 (branchId, buildNumber, state) VALUES (:branchId, :buildNumber, :state)")
  long enqueue(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE repo_builds_v2 SET startTimestamp = :startTimestamp, sha = :sha, state = :state, commitInfo = :commitInfo WHERE id = :id AND state = 'QUEUED'")
  int begin(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE repo_builds_v2 SET state = :state WHERE id = :id AND state = 'LAUNCHING'")
  int update(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE repo_builds_v2 SET endTimestamp = :endTimestamp, state = :state WHERE id = :id AND state IN ('QUEUED', 'LAUNCHING', 'IN_PROGRESS')")
  int complete(@BindWithRosetta RepositoryBuild build);
}
