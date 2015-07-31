package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.BuildDefinition;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.Set;

public interface BuildDefinitionDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, module.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId)")
  Set<BuildDefinition> getAllBuildDefinitions();
}
