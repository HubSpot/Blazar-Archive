package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface BranchDao {

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO branches (host, organization, repository, repositoryId, branch, active) VALUES (:host, :organization, :repository, :repositoryId, :branch, :active) ON DUPLICATE KEY UPDATE organization = :organization, repository = :repository, active = :active")
  long upsert(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("UPDATE branches SET active = 0 WHERE repositoryId = :repositoryId AND branch = :branch")
  int delete(@BindWithRosetta GitInfo gitInfo);
}
