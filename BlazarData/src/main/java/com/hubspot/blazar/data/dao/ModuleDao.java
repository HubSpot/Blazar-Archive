package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.GitInfo;
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

  @SqlQuery("SELECT m.id, m.name, m.path, m.glob, m.active from modules as m " +
      "INNER JOIN branches as b on (b.id = m.branchId) " +
      "WHERE b.host = :host " +
      "AND b.organization = :organization " +
      "AND b.repository = :repository " +
      "AND b.branch = :branch " +
      "AND m.name = :module")
  Optional<Module> getByInfo(@BindWithRosetta GitInfo info, @Bind("module") String module);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO modules (branchId, name, path, glob, active) VALUES (:branchId, :name, :path, :glob, :active)")
  int insert(@Bind("branchId") int branchId, @BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET path = :path, glob = :glob, active = :active WHERE id = :id")
  int update(@BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET active = 0 WHERE id = :id")
  int delete(@Bind("id") int id);

  @SqlUpdate("UPDATE modules SET pendingBuildId = :id WHERE id = :moduleId AND pendingBuildId IS NULL")
  int updatePendingBuild(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE modules SET inProgressBuildId = :id, pendingBuildId = NULL WHERE id = :moduleId AND pendingBuildId = :id AND inProgressBuildId IS NULL")
  int updateInProgressBuild(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE modules SET lastBuildId = :id, inProgressBuildId = NULL WHERE id = :moduleId AND inProgressBuildId = :id")
  int updateLastBuild(@BindWithRosetta Build build);
}
