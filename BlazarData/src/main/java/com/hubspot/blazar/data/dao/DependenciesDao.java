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
import com.hubspot.blazar.base.ModuleDependency.Source;
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

  @SqlQuery("SELECT gitInfo.* " +
            "FROM modules AS module " +
            "    join branches AS gitInfo ON (gitInfo.id = module.branchId) " +
            "    join module_provides AS provides ON (module.id = provides.moduleId) " +
            "    join module_depends  AS depends ON (module.id = depends.moduleId) " +
            "WHERE (provides.version IS NULL OR depends.version IS NULL) AND gitInfo.active = 1 " +
            "GROUP BY gitInfo.id")
  Set<GitInfo> getBranchesWithNonVersionedDependencies();

  @SqlBatch("INSERT INTO module_provides (moduleId, name, version, source) VALUES (:moduleId, :name, :version, :source)")
  void insertProvidedDependencies(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlBatch("INSERT INTO module_depends (moduleId, name, version, source) VALUES (:moduleId, :name, :version, :source)")
  void insertDependencies(@BindWithRosetta Set<ModuleDependency> dependencies);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :moduleId")
  void deleteProvidedDependencies(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_provides WHERE moduleId = :moduleId AND source = :source")
  void deleteProvidedDependenciesBySource(@Bind("moduleId") int moduleId, Source source);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :moduleId")
  void deleteDependencies(@Bind("moduleId") int moduleId);

  @SqlUpdate("DELETE FROM module_depends WHERE moduleId = :moduleId AND source = :source")
  void deleteDependenciesBySource(@Bind("moduleId") int moduleId, Source source);
}
