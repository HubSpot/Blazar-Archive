package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.util.BuildNumbers;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface RepositoryBuildDao {

  @SqlQuery("" +
      "SELECT pendingBuild.buildNumber AS pendingBuildNumber, inProgressBuild.buildNumber AS inProgressBuildNumber, lastBuild.buildNumber AS lastBuildNumber" +
      "FROM branches_v2 b " +
      "LEFT OUTER JOIN repo_builds_v2 AS pendingBuild ON (b.pendingBuildId = pendingBuild.id) " +
      "LEFT OUTER JOIN repo_builds_v2 AS inProgressBuild ON (b.inProgressBuildId = inProgressBuild.id) " +
      "LEFT OUTER JOIN repo_builds_v2 AS lastBuild ON (b.lastBuildId = lastBuild.id) " +
      "WHERE b.id = :id")
  BuildNumbers getBuildNumbers(GitInfo gitInfo);
}
