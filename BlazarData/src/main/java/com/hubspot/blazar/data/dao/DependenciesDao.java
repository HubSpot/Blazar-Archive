package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface DependenciesDao {

  @SqlQuery("SELECT module_provides.* " +
            "FROM module_provides " +
            "INNER JOIN modules ON (module_provides.moduleId = modules.id) " +
            "WHERE modules.branchId = :id")
  Set<ModuleDependency> getProvides(@BindWithRosetta GitInfo gitInfo);

  @SqlQuery("SELECT module_depends.* " +
            "FROM module_depends " +
            "INNER JOIN modules ON (module_depends.moduleId = modules.id) " +
            "WHERE modules.branchId = :id")
  Set<ModuleDependency> getDepends(@BindWithRosetta GitInfo gitInfo);

  @SqlBatch("INSERT INTO module_provides (moduleId, name) VALUES (:moduleId, :name)")
  void insertProvides(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlBatch("INSERT INTO module_depends (moduleId, name) VALUES (:moduleId, :name)")
  void insertDepends(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :moduleId")
  void deleteProvides(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :moduleId")
  void deleteDepends(@Bind("moduleId") int moduleId);
}
