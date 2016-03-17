package com.hubspot.blazar.data.dao;

import java.util.Collection;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

@UseStringTemplate3StatementLocator
public interface DependenciesDao {

  @SqlQuery("SELECT module_provides.* " +
            "FROM module_provides " +
            "INNER JOIN modules ON (module_provides.moduleId = modules.id) " +
            "WHERE modules.branchId = :id")
  Set<ModuleDependency> getProvides(@BindWithRosetta GitInfo gitInfo);

  @SqlQuery("SELECT * FROM module_provides WHERE moduleId in (<idList>)")
  Set<ModuleDependency> getProvides(@BindIn("idList") Collection<Integer> idList);

  @SqlQuery("SELECT module_depends.* " +
            "FROM module_depends " +
            "INNER JOIN modules ON (module_depends.moduleId = modules.id) " +
            "WHERE modules.branchId = :id")
  Set<ModuleDependency> getDepends(@BindWithRosetta GitInfo gitInfo);

  @SqlQuery("SELECT module_provides.name, module_provides.moduleId FROM module_provides "+
            "JOIN module_depends ON (module_provides.moduleId = module_depends.moduleId) "+
            "JOIN modules ON (module_depends.moduleId = modules.id) "+
            "JOIN branches ON (modules.branchId = branches.id) "+
            "WHERE module_depends.name = :name "+
            "and branches.active = 1")
  Set<ModuleDependency> getProvidesFromModuleDepends(@BindWithRosetta ModuleDependency moduleDependency);

  @SqlBatch("INSERT INTO module_provides (moduleId, name) VALUES (:moduleId, :name)")
  void insertProvides(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlBatch("INSERT INTO module_depends (moduleId, name) VALUES (:moduleId, :name)")
  void insertDepends(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :moduleId")
  void deleteProvides(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :moduleId")
  void deleteDepends(@Bind("moduleId") int moduleId);
}
