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

  @SqlQuery("SELECT * FROM branches where active = 1")
  Set<GitInfo> getAllActive();

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE id = :id")
  Optional<GitInfo> get(@Bind("id") int id);

  @SqlQuery("SELECT * FROM branches WHERE repositoryId = :repositoryId")
  Set<GitInfo> getByRepository(@Bind("repositoryId") int repositoryId);

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE repositoryId = :repositoryId AND branch = :branch")
  Optional<GitInfo> getByRepositoryAndBranch(@Bind("repositoryId") int repositoryId, @Bind("branch") String branch);

  /**
   * When repositories are re-named or moved between organizations Blazar does not get a webhook.
   * This means that there can be a discrepancy between the namespace that Blazar thinks a repository
   * lives in, and the namespace that it currently occupies on GitHub.
   *
   * When a repository is pushed to, after its name (host, org, repo) are changed Blazar can update
   * its database accordingly because the GitHub repositoryId does not change. However if a new
   * repository takes on the name of the old repository then searches by name will return branches
   * from both repositories.
   *
   * This query returns all the (conflicting) active branches Blazar knows of that have the same host,
   * org and repositoryName but have different GitHub repositoryIds than the GitInfo argument.
   *
   */
  @SqlQuery("" +
      "SELECT * FROM branches WHERE active = 1 AND " +
      "repositoryId != :repositoryId AND " +
      "host = :host AND " +
      "organization = :organization AND " +
      "repository = :repository")
  Set<GitInfo> getConflictingBranches(@BindWithRosetta GitInfo gitInfo);

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

  @SqlUpdate("UPDATE branches SET updatedTimestamp = NOW() WHERE id = :id")
  int touch(@Bind("id") int id);
}
