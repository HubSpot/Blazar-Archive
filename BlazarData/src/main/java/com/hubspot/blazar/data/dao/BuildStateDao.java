package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.Module;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.Set;

public interface BuildStateDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId) " +
      "LEFT OUTER JOIN builds AS lastBuild ON (module.lastBuild = lastBuild.id) " +
      "LEFT OUTER JOIN builds AS inProgressBuild ON (module.inProgressBuild = inProgressBuild.id) " +
      "LEFT OUTER JOIN builds AS pendingBuild ON (module.pendingBuild = pendingBuild.id)")
  Set<BuildState> getAllBuildStates();

  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId) " +
      "LEFT OUTER JOIN builds AS lastBuild ON (module.lastBuild = lastBuild.id) " +
      "LEFT OUTER JOIN builds AS inProgressBuild ON (module.inProgressBuild = inProgressBuild.id) " +
      "LEFT OUTER JOIN builds AS pendingBuild ON (module.pendingBuild = pendingBuild.id) " +
      "WHERE module.id = :id")
  BuildState getByModule(@BindWithRosetta Module module);
}
