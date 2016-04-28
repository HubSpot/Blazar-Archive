package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.blazar.base.graph.Edge;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import java.util.Set;

@UseStringTemplate3StatementLocator
public interface DependenciesDao {

  @SqlQuery("SELECT module_provides.moduleId AS source, module_depends.moduleId AS target " +
      "FROM module_provides " +
      "INNER JOIN module_depends ON (module_provides.name = module_depends.name) " +
      "INNER JOIN modules provides ON (module_provides.moduleId = provides.id) " +
      "INNER JOIN modules depends ON (module_provides.moduleId = depends.id) " +
      "WHERE provides.branchId = :id AND depends.branchId = :id")
  Set<Edge> getEdges(@BindWithRosetta GitInfo gitInfo);

  @SqlQuery("SELECT module_provides.moduleId AS source, module_depends.moduleId AS target " +
      "FROM module_provides " +
      "INNER JOIN module_depends ON (module_provides.name = module_depends.name) " +
      "INNER JOIN modules ON (module_depends.moduleId = modules.id) " +
      "INNER JOIN branches ON (modules.branchId = branches.id) " +
      "WHERE module_provides.moduleId IN (<moduleIds>) " +
      "AND modules.active = 1 " +
      "AND branches.active = 1")
  Set<Edge> getEdges(@BindIn("moduleIds") Set<Integer> moduleIds);

  @SqlBatch("INSERT INTO module_provides (moduleId, name) VALUES (:moduleId, :name)")
  void insertProvides(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlBatch("INSERT INTO module_depends (moduleId, name) VALUES (:moduleId, :name)")
  void insertDepends(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :moduleId")
  void deleteProvides(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :moduleId")
  void deleteDepends(@Bind("moduleId") int moduleId);
}
