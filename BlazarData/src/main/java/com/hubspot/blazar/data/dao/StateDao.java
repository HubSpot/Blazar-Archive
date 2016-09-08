package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildInfo;
import com.hubspot.blazar.base.RepositoryState;

public interface StateDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "LEFT OUTER JOIN repo_builds AS lastBuild ON (gitInfo.lastBuildId = lastBuild.id) " +
      "LEFT OUTER JOIN repo_builds AS inProgressBuild ON (gitInfo.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN repo_builds AS pendingBuild ON (gitInfo.pendingBuildId = pendingBuild.id) " +
      "WHERE gitInfo.active = 1")
  Set<RepositoryState> getAllRepositoryStates();

  @SqlQuery("" +
      "SELECT gitInfo.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "LEFT OUTER JOIN repo_builds AS lastBuild ON (gitInfo.lastBuildId = lastBuild.id) " +
      "LEFT OUTER JOIN repo_builds AS inProgressBuild ON (gitInfo.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN repo_builds AS pendingBuild ON (gitInfo.pendingBuildId = pendingBuild.id) " +
      "WHERE gitInfo.updatedTimestamp >= FROM_UNIXTIME(:since / 1000)")
  Set<RepositoryState> getChangedRepositoryStates(@Bind("since") long since);

  @SingleValueResult
  @SqlQuery("" +
      "SELECT gitInfo.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "LEFT OUTER JOIN repo_builds AS lastBuild ON (gitInfo.lastBuildId = lastBuild.id) " +
      "LEFT OUTER JOIN repo_builds AS inProgressBuild ON (gitInfo.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN repo_builds AS pendingBuild ON (gitInfo.pendingBuildId = pendingBuild.id) " +
      "WHERE gitInfo.id = :branchId")
  Optional<RepositoryState> getRepositoryState(@Bind("branchId") int branchId);

  @SingleValueResult
  @SqlQuery("" +
     "SELECT repositoryBuild.*, lastSuccessfulBuild.* "+
      "FROM module_builds AS lastSuccessfulBuild " +
      "LEFT OUTER JOIN repo_builds AS repositoryBuild on (lastSuccessfulBuild.repoBuildId = repositoryBuild.id) " +
      "WHERE lastSuccessfulBuild.moduleId = :moduleId " +
      "AND lastSuccessfulBuild.state = 'SUCCEEDED' " +
      "ORDER BY lastSuccessfulBuild.id DESC LIMIT 1")
  Optional<BuildInfo> getLastSuccessfulBuildInfo(@Bind("moduleId") int moduleId);

  @SingleValueResult
  @SqlQuery("" +
      "SELECT repositoryBuild.*, lastNonSkippedBuild.* " +
      "FROM module_builds AS lastNonSkippedBuild " +
      "LEFT OUTER JOIN repo_builds AS repositoryBuild on (lastNonSkippedBuild.repoBuildId = repositoryBuild.id) " +
      "WHERE lastNonSkippedBuild.moduleId = :moduleId " +
      "AND lastNonSkippedBuild.state in ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD', 'LAUNCHING', 'IN_PROGRESS', 'SUCCEEDED', 'CANCELLED', 'FAILED') " +
      "ORDER BY lastNonSkippedBuild.id DESC LIMIT 1")
  Optional<BuildInfo> getLastNonSkippedBuildInfo(@Bind("moduleId") int moduleId);

  @SingleValueResult
  @SqlQuery("" +
      "SELECT repositoryBuild.*, inProgressBuild.* " +
      "FROM module_builds AS inProgressBuild " +
      "LEFT OUTER JOIN repo_builds AS repositoryBuild on (inProgressBuild.repoBuildId = repositoryBuild.id) " +
      "WHERE inProgressBuild.id = :inProgressBuildId")
  Optional<BuildInfo> getInProgressBuildInfo(@Bind("inProgressBuildId") long inProgressBuildId);

  @SingleValueResult
  @SqlQuery("" +
      "SELECT repositoryBuild.*, lastBuild.* " +
      "FROM module_builds AS lastBuild " +
      "LEFT OUTER JOIN repo_builds AS repositoryBuild on (lastBuild.repoBuildId = repositoryBuild.id) " +
      "WHERE lastBuild.id = :lastBuildId")
  Optional<BuildInfo> getLastBuildInfo(@Bind("lastBuildId") long lastBuildId);

  @SingleValueResult
  @SqlQuery("" +
      "SELECT repositoryBuild.*, pendingBuild.* " +
      "FROM module_builds AS pendingBuild " +
      "LEFT OUTER JOIN repo_builds AS repositoryBuild on (pendingBuild.repoBuildId = repositoryBuild.id) " +
      "WHERE pendingBuild.id = :pendingBuildId")
  Optional<BuildInfo> getPendingBuildInfo(@Bind("pendingBuildId") long pendingBuildId);
}
