package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface ModuleDao {
  String NOW = "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)";

  @SqlQuery("SELECT * modules_v2 WHERE branchId = :branchId")
  Set<Module> getByBranch(@Bind("branchId") int branchId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO modules_v2 (branchId, name, type, path, glob, active, buildpack, createdTimestamp, updatedTimestamp) VALUES (:branchId, :name, :type, :path, :glob, :active, :buildpack, " + NOW + ", " + NOW + ")")
  int insert(@Bind("branchId") int branchId, @BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules_v2 SET path = :path, glob = :glob, active = :active, buildpack = :buildpack, updatedTimestamp = " + NOW + " WHERE id = :id")
  int update(@BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules_v2 SET active = 0, updatedTimestamp = " + NOW + " WHERE id = :id")
  int delete(@Bind("id") int id);

  @SqlUpdate("UPDATE modules_v2 SET pendingBuildId = :id, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND pendingBuildId IS NULL")
  int updatePendingBuild(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE modules_v2 SET pendingBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND pendingBuildId = :id")
  int deletePendingBuild(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE modules_v2 SET inProgressBuildId = :id, pendingBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND pendingBuildId = :id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta ModuleBuild build);

  @SqlUpdate("UPDATE modules_v2 SET lastBuildId = :id, inProgressBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND inProgressBuildId = :id")
  int updateLastBuild(@BindWithRosetta ModuleBuild build);
}
