package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.util.Set;

public interface BuildStateDao {

  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, lastBuild.*, inProgressBuild.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId) " +
      "LEFT OUTER JOIN builds AS lastBuild ON (module.lastBuild = lastBuild.id) " +
      "LEFT OUTER JOIN builds AS inProgressBuild ON (module.inProgressBuild = inProgressBuild.id)")
  Set<BuildState> getAllBuildStates();

  @SingleValueResult
  @SqlQuery("" +
      "SELECT build.*, module.*, gitInfo.* " +
      "FROM builds AS build " +
      "INNER JOIN modules AS module ON (module.id = build.moduleId) " +
      "INNER JOIN branches AS gitInfo ON (gitInfo.id = module.branchId) " +
      "WHERE build.id = :it")
  Optional<ModuleBuild> get(@Bind long id);

  @SqlUpdate("UPDATE builds SET state = :state, endTimestamp = :endTimestamp, log = :log WHERE id = :id")
  int update(@BindWithRosetta Build build);
}
