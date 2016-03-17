package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface BranchDao {

  @SqlQuery("SELECT * FROM branches")
  Set<GitInfo> getAll();

  @SqlQuery("Select * from branches WHERE branch = :branch")
  Set<GitInfo> getAllOnBranch(@Bind("branch") String branch);

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE id = :id")
  Optional<GitInfo> get(@Bind("id") int id);

  @SqlQuery("SELECT * FROM branches WHERE repositoryId = :repositoryId")
  Set<GitInfo> getByRepository(@Bind("repositoryId") int repositoryId);

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE repositoryId = :repositoryId AND branch = :branch")
  Optional<GitInfo> getByRepositoryAndBranch(@Bind("repositoryId") int repositoryId, @Bind("branch") String branch);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO branches (host, organization, repository, repositoryId, branch, active) VALUES (:host, :organization, :repository, :repositoryId, :branch, :active)")
  int insert(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET organization = :organization, repository = :repository, active = :active WHERE id = :id")
  int update(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET active = 0 WHERE repositoryId = :repositoryId AND branch = :branch")
  int delete(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET pendingBuildId = :id WHERE id = :branchId AND pendingBuildId IS NULL")
  int updatePendingBuild(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE branches SET pendingBuildId = :next.id WHERE id = :previous.branchId AND id = :next.branchId AND pendingBuildId = :previous.id")
  int updatePendingBuild(@BindWithRosetta("previous") RepositoryBuild previous, @BindWithRosetta("next") RepositoryBuild next);

  @SqlUpdate("UPDATE branches SET pendingBuildId = NULL WHERE id = :branchId AND pendingBuildId = :id")
  int deletePendingBuild(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE branches SET inProgressBuildId = :id, pendingBuildId = NULL WHERE id = :branchId AND pendingBuildId = :id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta RepositoryBuild build);

  @SqlUpdate("UPDATE branches SET inProgressBuildId = :current.id, pendingBuildId = :next.id WHERE id = :current.branchId AND id = :next.branchId AND pendingBuildId = :current.id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta("current") RepositoryBuild current, @BindWithRosetta("next") RepositoryBuild next);

  @SqlUpdate("UPDATE branches SET lastBuildId = :id, inProgressBuildId = NULL WHERE id = :branchId AND inProgressBuildId = :id")
  int updateLastBuild(@BindWithRosetta RepositoryBuild build);
}
