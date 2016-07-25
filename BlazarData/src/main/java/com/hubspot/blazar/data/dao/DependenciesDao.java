package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import com.hubspot.blazar.base.Dependency;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.blazar.base.graph.Edge;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

@UseStringTemplate3StatementLocator
public interface DependenciesDao {

  @SqlQuery("SELECT module_provides.moduleId AS source, module_depends.moduleId AS target " +
      "FROM module_provides " +
      "INNER JOIN module_depends ON (module_provides.name = module_depends.name) " +
      "INNER JOIN modules ON (module_depends.moduleId = modules.id) " +
      "INNER JOIN branches ON (modules.branchId = branches.id) " +
      "LEFT JOIN branch_settings on (branches.id = branch_settings.branchId) " +
      "WHERE module_provides.moduleId IN (<moduleIds>) " +
      "AND branches.active = 1 " +
      "AND (" +
      "  branch_settings.interProjectBuildOptIn = 1 " +
      "  OR (branches.branch = 'master' AND branch_settings.interProjectBuildOptIn IS NULL) " +
      "  OR branches.id = :branchId" +
      ")")
  Set<Edge> getEdges(@Bind("branchId") int branchId, @BindIn("moduleIds") Set<Integer> moduleIds);

  @SqlQuery("SELECT * FROM module_provides WHERE moduleId = :moduleId")
  Set<Dependency> getProvided(@Bind("moduleId") int moduleId);

  @SqlQuery("SELECT * FROM module_depends WHERE moduleId = :moduleId")
  Set<Dependency> getDependencies(@Bind("moduleId") int moduleId);

  @SqlQuery("select branch.* " +
            "    from modules as module " +
            "        join branches as branch on (branch.id = module.branchId) " +
            "        join module_provides as provides on (module.id = provides.moduleId) " +
            "        join module_depends  as depends on (module.id = depends.moduleId) " +
            "    where (provides.version is null or depends.version is null) and  branch.active = 1 group by branch.id")
  Set<GitInfo> getBranchesWithNonVersionedDependencies();

  @SqlBatch("INSERT INTO module_provides (moduleId, name, version) VALUES (:moduleId, :name, :version)")
  void insertProvides(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlBatch("INSERT INTO module_depends (moduleId, name, version) VALUES (:moduleId, :name, :version)")
  void insertDepends(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :moduleId")
  void deleteProvides(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :moduleId")
  void deleteDepends(@Bind("moduleId") int moduleId);
}
