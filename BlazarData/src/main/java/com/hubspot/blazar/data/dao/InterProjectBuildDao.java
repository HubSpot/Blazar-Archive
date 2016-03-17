package com.hubspot.blazar.data.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface InterProjectBuildDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM inter_project_builds WHERE id = :id")
  Optional<InterProjectBuild> getWithId(@Bind("id") long id);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO inter_project_builds (state, moduleIds, buildTrigger) VALUES (:state, :moduleIds, :buildTrigger)")
  int enqueue(@BindWithRosetta InterProjectBuild interProjectBuild);

  @SqlUpdate("UPDATE inter_project_builds SET " +
             "state = :state, " +
             "startTimestamp = :startTimestamp, " +
             "dependencyGraph = :dependencyGraph " +
             "WHERE id = :id and state in ('CALCULATING')")
  void start(@BindWithRosetta InterProjectBuild interProjectBuild);

  @SqlUpdate("UPDATE inter_project_builds SET " +
             "state = :state, " +
             "endTimestamp = :endTimestamp " +
             "WHERE id = :id and state in ('RUNNING')")
  void finish(@BindWithRosetta InterProjectBuild interProjectBuild);
}
