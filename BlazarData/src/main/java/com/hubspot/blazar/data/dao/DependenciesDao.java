package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface DependenciesDao {

  @SqlBatch("INSERT INTO module_provides (moduleId, name) VALUES (:moduleId, :name)")
  void insertProvides(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlBatch("INSERT INTO module_depends (moduleId, name) VALUES (:moduleId, :name)")
  void insertDepends(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :it")
  void deleteProvides(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :it")
  void deleteDepends(@Bind("moduleId") int moduleId);
}
