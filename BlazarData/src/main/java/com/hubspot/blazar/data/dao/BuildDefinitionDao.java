package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildDefinition;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.util.Set;

public interface BuildDefinitionDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, module.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId)")
  Set<BuildDefinition> getAllBuildDefinitions();

  @SingleValueResult
  @SqlQuery("" +
      "SELECT gitInfo.*, module.* " +
      "FROM modules AS module " +
      "INNER JOIN branches AS gitInfo ON (gitInfo.id = module.branchId) " + "" +
      "WHERE module.id = :it")
  Optional<BuildDefinition> getByModuleId(@Bind int moduleId);
}
