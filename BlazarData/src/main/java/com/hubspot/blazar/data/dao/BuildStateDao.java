package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.BuildState;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.Set;

public interface BuildStateDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, lastBuild.*, inProgressBuild.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId) " +
      "LEFT OUTER JOIN builds AS lastBuild ON (module.lastBuild = lastBuild.id) " +
      "LEFT OUTER JOIN builds AS inProgressBuild ON (module.inProgressBuild = inProgressBuild.id)")
  Set<BuildState> getAllBuildStates();
}
