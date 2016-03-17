package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface ModuleDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM modules WHERE id = :moduleId")
  Optional<Module> get(@Bind("moduleId") int moduleId);

  @SqlQuery("SELECT * FROM modules WHERE branchId = :branchId")
  Set<Module> getByBranch(@Bind("branchId") int branchId);

  @SqlQuery("SELECT branchId FROM modules WHERE id = :moduleId")
  int getBranchIdFromModuleId(@Bind("moduleId") int moduleId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO modules (branchId, name, type, path, glob, active, buildpack) VALUES (:branchId, :name, :type, :path, :glob, :active, :buildpack)")
  int insert(@Bind("branchId") int branchId, @BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET path = :path, glob = :glob, active = :active, buildpack = :buildpack WHERE id = :id")
  int update(@BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET active = 0 WHERE id = :id")
  int delete(@Bind("id") int id);

  @SqlUpdate("UPDATE modules SET pendingBuildId = :id WHERE id = :moduleId AND pendingBuildId IS NULL")
  int updatePendingBuild(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE modules SET inProgressBuildId = :id, pendingBuildId = NULL WHERE id = :moduleId AND pendingBuildId = :id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE modules SET lastBuildId = :id, inProgressBuildId = NULL WHERE id = :moduleId AND inProgressBuildId = :id")
  int updateLastBuild(@BindWithRosetta ModuleBuild build);
}
