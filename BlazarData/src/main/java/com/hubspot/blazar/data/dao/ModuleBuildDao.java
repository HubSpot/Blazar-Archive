package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.util.List;
import java.util.Set;

public interface ModuleBuildDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM module_builds WHERE id = :id")
  Optional<ModuleBuild> get(@Bind("id") long id);

  @SqlQuery("SELECT * FROM module_builds WHERE repoBuildId = :repoBuildId")
  Set<ModuleBuild> getByRepositoryBuild(@Bind("repoBuildId") long repoBuildId);

  @SqlQuery("SELECT * FROM module_builds WHERE state = :state")
  Set<ModuleBuild> getByState(@Bind("state") State state);

  @SqlQuery("SELECT * FROM module_builds WHERE moduleId = :moduleId ORDER BY id DESC")
  List<ModuleBuild> getByModule(@Bind("moduleId") int moduleId);

  @SingleValueResult
  @SqlQuery("SELECT * FROM module_builds WHERE moduleId = :moduleId AND buildNumber = :buildNumber")
  Optional<ModuleBuild> getByModuleAndNumber(@Bind("moduleId") int moduleId, @Bind("buildNumber") int buildNumber);

  @SqlQuery("" +
      "SELECT pendingBuild.id AS pendingBuildId, " +
      "pendingBuild.buildNumber AS pendingBuildNumber, " +
      "inProgressBuild.id AS inProgressBuildId, " +
      "inProgressBuild.buildNumber AS inProgressBuildNumber, " +
      "lastBuild.id AS lastBuildId, " +
      "lastBuild.buildNumber AS lastBuildNumber " +
      "FROM modules m " +
      "LEFT OUTER JOIN module_builds AS pendingBuild ON (m.pendingBuildId = pendingBuild.id) " +
      "LEFT OUTER JOIN module_builds AS inProgressBuild ON (m.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN module_builds AS lastBuild ON (m.lastBuildId = lastBuild.id) " +
      "WHERE m.id = :moduleId")
  BuildNumbers getBuildNumbers(@Bind("moduleId") int moduleId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO module_builds (repoBuildId, moduleId, buildNumber, state) VALUES (:repoBuildId, :moduleId, :buildNumber, :state)")
  long enqueue(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE module_builds SET startTimestamp = :startTimestamp, state = :state, buildConfig = :buildConfig, resolvedConfig = :resolvedConfig WHERE id = :id AND state = 'QUEUED'")
  int begin(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE module_builds SET taskId = :taskId, state = :state WHERE id = :id AND state IN ('LAUNCHING', 'IN_PROGRESS')")
  int update(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE module_builds SET endTimestamp = :endTimestamp, state = :state WHERE id = :id AND state IN ('QUEUED', 'LAUNCHING', 'IN_PROGRESS')")
  int complete(@BindWithRosetta ModuleBuild build);
}