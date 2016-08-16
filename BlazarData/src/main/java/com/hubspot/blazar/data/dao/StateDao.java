package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

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
      "SELECT module.*, " +
      "     lastSuccessfulBuild.*, " +
      "     lastNonSkippedBuild.*, " +
      "     lastBuild.*, " +
      "     inProgressBuild.*, " +
      "     pendingBuild.* " +
      "  FROM modules AS module " +
      "     LEFT OUTER JOIN module_builds AS lastSuccessfulBuild ON (module.id = lastSuccessfulBuild.moduleId) " +
      "     LEFT OUTER JOIN module_builds AS lastNonSkippedBuild ON (module.id = lastNonSkippedBuild.moduleId) " +
      "     LEFT OUTER JOIN module_builds AS lastBuild ON (module.lastBuildId = lastBuild.id) " +
      "     LEFT OUTER JOIN module_builds AS inProgressBuild ON (module.inProgressBuildId = inProgressBuild.id) " +
      "     LEFT OUTER JOIN module_builds AS pendingBuild ON (module.pendingBuildId = pendingBuild.id) " +
      "  WHERE module.id = :id " +
      "  AND lastNonSkippedBuild.state != 'SKIPPED' " +
      "  AND lastSuccessfulBuild.state = 'SUCCEEDED' " +
      "  ORDER BY lastNonSkippedBuild.id DESC, lastSuccessfulBuild.id DESC LIMIT 1")
  Optional<ModuleState> getModuleStateById(@BindWithRosetta Module module);
}
