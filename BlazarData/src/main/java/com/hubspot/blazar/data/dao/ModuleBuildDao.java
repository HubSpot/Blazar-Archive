package com.hubspot.blazar.data.dao;

import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface ModuleBuildDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM module_builds WHERE id = :id")
  Optional<ModuleBuild> get(@Bind("id") long id);

  @SqlQuery("SELECT * FROM module_builds WHERE repoBuildId = :repoBuildId")
  Set<ModuleBuild> getByRepositoryBuild(@Bind("repoBuildId") long repoBuildId);

  @SqlQuery("SELECT * FROM module_builds WHERE state = :state")
  Set<ModuleBuild> getByState(@Bind("state") State state);

  @SqlQuery("" +
      "SELECT * FROM module_builds AS moduleBuild " +
      "LEFT OUTER JOIN repo_builds AS branchBuild ON moduleBuild.repoBuildId = branchBuild.id  " +
      "WHERE moduleId = :moduleId " +
      "AND moduleBuild.buildNumber <= :buildNumber " +
      "ORDER BY moduleBuild.buildNumber " +
      "LIMIT :limit")
  List<ModuleBuildInfo> getLimitedModuleBuildHistory(@Bind("moduleId") int moduleId, @Bind("buildNumber") int buildNumber, @Bind("limit") int limit);

  @SingleValueResult
  @SqlQuery("" +
      "SELECT COUNT(*) AS count FROM module_builds AS moduleBuild " +
      "WHERE moduleId = :moduleId " +
      "AND moduleBuild.buildNumber <= :buildNumberForPageStart")
  Optional<Integer> getRemainingBuildCountForPagedHistory(@Bind("moduleId") int moduleId, @Bind("buildNumberForPageStart") int buildNumberForPageStart);

  @SingleValueResult
  @SqlQuery("SELECT * FROM module_builds WHERE moduleId = :moduleId AND buildNumber = :buildNumber")
  Optional<ModuleBuild> getByModuleAndNumber(@Bind("moduleId") int moduleId, @Bind("buildNumber") int buildNumber);

  @SingleValueResult
  @SqlQuery("SELECT * FROM module_builds WHERE moduleId = :moduleId AND buildNumber < :buildNumber AND state != 'SKIPPED' ORDER BY buildNumber DESC LIMIT 1")
  Optional<ModuleBuild> getPreviousBuild(@BindWithRosetta ModuleBuild build);

  @SingleValueResult
  @SqlQuery("SELECT * FROM module_builds WHERE moduleId = :id AND state != 'SKIPPED' ORDER BY buildNumber DESC LIMIT 1")
  Optional<ModuleBuild> getPreviousBuild(@BindWithRosetta Module module);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO module_builds (repoBuildId, moduleId, buildNumber, state) VALUES (:repoBuildId, :moduleId, :buildNumber, :state)")
  long skip(@BindWithRosetta ModuleBuild build);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO module_builds (repoBuildId, moduleId, buildNumber, state) VALUES (:repoBuildId, :moduleId, :buildNumber, :state)")
  long enqueue(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE module_builds SET startTimestamp = :startTimestamp, state = :state, buildConfig = :buildConfig, resolvedConfig = :resolvedConfig WHERE id = :id AND state IN ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD')")
  int begin(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE module_builds SET taskId = :taskId, state = :state WHERE id = :id AND state IN ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD', 'LAUNCHING', 'IN_PROGRESS')")
  int update(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE module_builds SET endTimestamp = :endTimestamp, taskId = :taskId, state = :state WHERE id = :id AND state IN ('QUEUED', 'WAITING_FOR_UPSTREAM_BUILD', 'LAUNCHING', 'IN_PROGRESS')")
  int complete(@BindWithRosetta ModuleBuild build);
}
