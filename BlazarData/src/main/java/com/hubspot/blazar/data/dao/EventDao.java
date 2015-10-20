package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.Event;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.List;

public interface EventDao {
  
  @SqlUpdate("INSERT INTO events (moduleId, timestamp, username) VALUES (:moduleId, :timestamp, :username)")
  int insert(@BindWithRosetta Event event);

  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, lastBuild.*, inProgressBuild.*, pendingBuild.* " +
      "FROM branches AS gitInfo " +
      "INNER JOIN modules AS module ON (gitInfo.id = module.branchId) " +
      "LEFT OUTER JOIN builds AS lastBuild ON (module.lastBuildId = lastBuild.id) " +
      "LEFT OUTER JOIN builds AS inProgressBuild ON (module.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN builds AS pendingBuild ON (module.pendingBuildId = pendingBuild.id) " +
      "LEFT OUTER JOIN events AS events ON (module.id = events.moduleId)" +
      "LEFT OUTER JOIN branches AS branches ON (module.branchid = branches.id)" +
      "WHERE events.timestamp >= :since" +
      "AND module.host = :gitHubHost" +
      "AND events.username = :username")
  List<BuildState> getAllEventsForUser(@Bind("username") String username, @Bind("gitHubHost") String gitHubHost, @Bind("since") long since);
}
