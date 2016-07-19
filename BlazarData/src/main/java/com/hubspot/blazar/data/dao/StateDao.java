package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleState;
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
      "WHERE gitInfo.active = 1 AND "+
      "gitInfo.host = :host AND " +
      "gitInfo.organization = :org AND " +
      "gitInfo.repository = :repo")
  Set<RepositoryState> getStatesForRepoByNames(@Bind("host") String host, @Bind("org") String org, @Bind("repo") String repo);

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

  @SqlQuery("" +
      "SELECT module.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM modules AS module " +
      "LEFT OUTER JOIN module_builds AS lastBuild ON (module.lastBuildId = lastBuild.id) " +
      "LEFT OUTER JOIN module_builds AS inProgressBuild ON (module.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN module_builds AS pendingBuild ON (module.pendingBuildId = pendingBuild.id) " +
      "WHERE module.branchId = :branchId")
  Set<ModuleState> getModuleStatesByBranch(@Bind("branchId") int branchId);
}
