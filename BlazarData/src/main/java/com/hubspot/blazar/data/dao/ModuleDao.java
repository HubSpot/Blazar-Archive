package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Module;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface ModuleDao {
  String NOW = "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)";

  @SqlQuery("SELECT id, name, path, glob, active, updatedTimestamp FROM modules WHERE branchId = :branchId")
  Set<Module> getByBranch(@Bind("branchId") int branchId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO modules (branchId, name, path, glob, active, updatedTimestamp) VALUES (:branchId, :name, :path, :glob, :active, " + NOW + ")")
  int insert(@Bind("branchId") int branchId, @BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET path = :path, glob = :glob, active = :active, updatedTimestamp = " + NOW + " WHERE id = :id")
  int update(@BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET active = 0, updatedTimestamp = " + NOW + " WHERE id = :id")
  int delete(@Bind("id") int id);

  @SqlUpdate("UPDATE modules SET pendingBuildId = :id, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND pendingBuildId IS NULL")
  int updatePendingBuild(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE modules SET inProgressBuildId = :id, pendingBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND pendingBuildId = :id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE modules SET lastBuildId = :id, inProgressBuildId = NULL, updatedTimestamp = " + NOW + " WHERE id = :moduleId AND inProgressBuildId = :id")
  int updateLastBuild(@BindWithRosetta Build build);
}
