package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.Module;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface ModuleDao {

  @SqlQuery("SELECT id, name, path, active FROM modules WHERE branchId = :branchId")
  Set<Module> getByBranch(@Bind("branchId") long branchId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO modules (branchId, name, path, active) VALUES (:branchId, :name, :path, :active) ON DUPLICATE KEY UPDATE path = :path, active = :active")
  long upsert(@Bind("branchId") long branchId, @BindWithRosetta Module module);

  @SqlUpdate("UPDATE modules SET active = 0 WHERE id = :id")
  int delete(@Bind("id") long id);
}
