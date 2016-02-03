package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

public interface BranchDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE repositoryId = :repositoryId AND branch = :branch")
  Optional<GitInfo> get(@BindWithRosetta GitInfo gitInfo);

  @SingleValueResult
  @SqlQuery("SELECT * FROM branches WHERE host = :host AND organization = :organization AND repository = :repository AND branch = :branch")
  Optional<GitInfo> lookup(@Bind("host") String host, @Bind("organization") String organization, @Bind("repository") String repository, @Bind("branch") String branch);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO branches (host, organization, repository, repositoryId, branch, active) VALUES (:host, :organization, :repository, :repositoryId, :branch, :active)")
  int insert(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET organization = :organization, repository = :repository, active = :active WHERE id = :id")
  int update(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET active = 0 WHERE repositoryId = :repositoryId AND branch = :branch")
  int delete(@BindWithRosetta GitInfo gitInfo);
}
