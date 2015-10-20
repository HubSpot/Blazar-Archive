package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.BuildState;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.Set;

public interface BuildStateDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId) " +
      "LEFT OUTER JOIN builds AS lastBuild ON (module.lastBuildId = lastBuild.id) " +
      "LEFT OUTER JOIN builds AS inProgressBuild ON (module.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN builds AS pendingBuild ON (module.pendingBuildId = pendingBuild.id) " +
      "WHERE module.updatedTimestamp >= :since")
  Set<BuildState> getAllBuildStates(@Bind("since") long since);
}
