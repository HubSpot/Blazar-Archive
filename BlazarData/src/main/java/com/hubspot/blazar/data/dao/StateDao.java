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
      "SELECT * " +
      "  FROM modules AS module " +
      // last build
      "     LEFT OUTER JOIN module_builds AS lastModuleBuild ON (module.lastBuildId = lastModuleBuild.id) " +
      "     LEFT OUTER JOIN repo_builds AS lastRepoBuild ON (lastModuleBuild.repoBuildId = lastRepoBuild.id) " +
      // in progress build
      "     LEFT OUTER JOIN module_builds AS inProgressModuleBuild ON (module.inProgressBuildId = inProgressModuleBuild.id) " +
      "     LEFT OUTER JOIN repo_builds AS inProgressRepoBuild ON (inProgressModuleBuild.repoBuildId = inProgressRepoBuild.id) " +
      // pending build
      "     LEFT OUTER JOIN module_builds AS pendingModuleBuild ON (module.pendingBuildId =pendingModuleBuild.id) " +
      "     LEFT OUTER JOIN repo_builds AS pendingRepoBuild ON (pendingModuleBuild.repoBuildId = pendingRepoBuild.id) " +
      // last non skipped
      "     LEFT OUTER JOIN module_builds AS lastNonSkippedModuleBuild ON (module.id = lastNonSkippedModuleBuild.moduleId) " +
      "     LEFT OUTER JOIN repo_builds AS lastNonSkippedRepoBuild ON (lastNonSkippedModuleBuild.repoBuildId = lastNonSkippedRepoBuild.id) " +
      // last successful
      "     LEFT OUTER JOIN module_builds AS lastSuccessfulModuleBuild ON (module.id = lastSuccessfulModuleBuild.moduleId) " +
      "     LEFT OUTER JOIN repo_builds AS lastSuccessfulRepoBuild ON (lastSuccessfulModuleBuild.repoBuildId = lastSuccessfulRepoBuild.id) " +
      "  WHERE module.id = :moduleId " +
      // lastNonSkipped and lastSuccessful ids are gotten in subqueries to avoid sorting for the lastNonSkipped and then also lastSuccessful
      "  AND lastNonSkippedModuleBuild.id = (SELECT lastNonSkippedBuild.id FROM module_builds AS lastNonSkippedBuild " +
      "                                      LEFT OUTER JOIN repo_builds AS repositoryBuild ON (lastNonSkippedBuild.repoBuildId = repositoryBuild.id) " +
      "                                      WHERE lastNonSkippedBuild.moduleId = :moduleId " +
      "                                      AND lastNonSkippedBuild.state IN ('SUCCEEDED', 'CANCELLED', 'FAILED') " +
      "                                      ORDER BY lastNonSkippedBuild.id DESC LIMIT 1) " +
      "  AND lastSuccessfulModuleBuild.id = (SELECT lastSuccessfulBuild.id " +
      "                                      FROM module_builds AS lastSuccessfulBuild " +
      "                                      LEFT OUTER JOIN repo_builds AS repositoryBuild ON (lastSuccessfulBuild.repoBuildId = repositoryBuild.id) " +
      "                                      WHERE lastSuccessfulBuild.moduleId = :moduleId" +
      "                                      AND lastSuccessfulBuild.state = 'SUCCEEDED' " +
      "                                      ORDER BY lastSuccessfulBuild.id DESC LIMIT 1)")
  Optional<ModuleState> getModuleStateById(@Bind("moduleId") long moduleId);

}

