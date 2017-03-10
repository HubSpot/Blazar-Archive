package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuildInfo;
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

  /**
   * @param branchId The branch we are fetching module state for
   * @return Optional(ModuleState) (partial)
   * <p>
   * Complete state of a module is a very complex thing spanning many different builds
   * - lastSuccessful
   * - lastNonSkipped
   * - lastCompleted
   * - pending
   * - inProgress
   * <p>
   * To enhance performance, #getLastAndInProgressAndPendingBuildsForBranchAndIncludedModules retrieves the following data for each module on a branch:
   * - the module
   * - lastCompleted
   * - pending
   * - inProgress
   * <p>
   * It returns these in the form of a partially filled ModuleState object, A set of ModuleBuildInfo for
   * lastSuccessful and lastNonSkipped can be fetched with #getLastSuccessfulAndNonSkippedModuleBuilds.
   */
  @SqlQuery("" +
      "SELECT * " +
      "  FROM branches " +
      "     JOIN modules as module on (branches.id = module.branchId) " +
      // last build
      "     LEFT OUTER JOIN module_builds AS lastModuleBuild ON (module.lastBuildId = lastModuleBuild.id) " +
      "     LEFT OUTER JOIN repo_builds AS lastBranchBuild ON (lastModuleBuild.repoBuildId = lastBranchBuild.id) " +
      // in progress build
      "     LEFT OUTER JOIN module_builds AS inProgressModuleBuild ON (module.inProgressBuildId = inProgressModuleBuild.id) " +
      "     LEFT OUTER JOIN repo_builds AS inProgressBranchBuild ON (inProgressModuleBuild.repoBuildId = inProgressBranchBuild.id) " +
      // pending build
      "     LEFT OUTER JOIN module_builds AS pendingModuleBuild ON (module.pendingBuildId = pendingModuleBuild.id) " +
      "     LEFT OUTER JOIN repo_builds AS pendingBranchBuild ON (pendingModuleBuild.repoBuildId = pendingBranchBuild.id) " +
      "  WHERE branches.id = :branchId")
  Set<ModuleState> getLastAndInProgressAndPendingBuildsForBranchAndIncludedModules(@Bind("branchId") int branchId);

  /**
   * This query fetches build information for the lastSuccessful and lastNonSkipped builds for a module.
   * If they are the same this will return a set of 1 moduleBuildInfo, otherwise a set of 2 items.
   */
  @SqlQuery("" +
      "SELECT * FROM " +
      "((SELECT * FROM module_builds as lastNonSkipped WHERE lastNonSkipped.moduleId = :moduleId AND lastNonSkipped.state IN ('SUCCEEDED', 'CANCELLED', 'FAILED') ORDER BY lastNonSkipped.buildNumber DESC LIMIT 1) " +
      "UNION " +
      "(SELECT * FROM module_builds  as lastSuccessful WHERE moduleId = :moduleId AND lastSuccessful.state = 'SUCCEEDED' ORDER BY lastSuccessful.buildNumber DESC LIMIT 1)) AS moduleBuild " +
      "JOIN repo_builds AS branchBuild ON (branchBuild.id = moduleBuild.repoBuildId)")
  Set<ModuleBuildInfo> getLastSuccessfulAndNonSkippedModuleBuilds(@Bind("moduleId") int moduleId);
}

