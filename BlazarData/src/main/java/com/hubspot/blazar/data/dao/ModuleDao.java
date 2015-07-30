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

  @SqlQuery("SELECT id, name, path, glob, active FROM modules WHERE branchId = :branchId")
  Set<Module> getByBranch(@Bind("branchId") int branchId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO modules (branchId, name, path, glob, active) VALUES (:branchId, :name, :path, :glob, :active)")
  int insert(@Bind("branchId") int branchId, @BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET path = :path, glob = :glob, active = :active WHERE id = :id")
  int update(@BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET active = 0 WHERE id = :id")
  int delete(@Bind("id") int id);

  @SqlUpdate("UPDATE modules SET pendingBuild = :id WHERE id = :moduleId AND pendingBuild IS NULL")
  int updatePendingBuild(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE modules SET inProgressBuild = :id, pendingBuild = NULL WHERE id = :moduleId AND pendingBuild = :id AND inProgressBuild IS NULL")
  int updateInProgressBuild(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE modules SET lastBuild = :id, inProgressBuild = NULL WHERE id = :moduleId AND inProgressBuild = :id")
  int updateLastBuild(@BindWithRosetta Build build);
}
