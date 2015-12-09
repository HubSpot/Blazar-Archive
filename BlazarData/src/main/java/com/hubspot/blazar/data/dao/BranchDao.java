package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

public interface BranchDao {
  String NOW = "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)";

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE id = :id")
  Optional<GitInfo> get(@Bind("id") int id);

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE repositoryId = :repositoryId AND branch = :branch")
  Optional<GitInfo> get(@BindWithRosetta GitInfo gitInfo);

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE host = :host AND organization = :organization AND repository = :repository AND branch = :branch")
  Optional<GitInfo> lookup(@Bind("host") String host, @Bind("organization") String organization, @Bind("repository") String repository, @Bind("branch") String branch);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO branches (host, organization, repository, repositoryId, branch, active, createdTimestamp, updatedTimestamp) VALUES (:host, :organization, :repository, :repositoryId, :branch, :active, " + NOW + ", " + NOW + ")")
  int insert(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET organization = :organization, repository = :repository, active = :active, updatedTimestamp = " + NOW + " WHERE id = :id")
  int update(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET active = 0, updatedTimestamp = " + NOW + " WHERE repositoryId = :repositoryId AND branch = :branch")
  int delete(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET pendingBuildId = :id, updatedTimestamp = " + NOW + " WHERE id = :branchId AND pendingBuildId IS NULL")
  int updatePendingBuild(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE branches SET inProgressBuildId = :id, pendingBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :branchId AND pendingBuildId = :id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE branches SET lastBuildId = :id, inProgressBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :branchId AND inProgressBuildId = :id")
  int updateLastBuild(@BindWithRosetta RepositoryBuild build);
}
