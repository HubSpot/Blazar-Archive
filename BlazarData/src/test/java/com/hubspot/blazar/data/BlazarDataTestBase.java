package com.hubspot.blazar.data;

import org.junit.After;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.test.base.service.BlazarTestBase;

import io.dropwizard.db.ManagedDataSource;

public class BlazarDataTestBase extends BlazarTestBase {

  @Inject
  ManagedDataSource dataSource;

  @After
  public void cleanup() throws Exception {
    runSql(dataSource, "cleanup.sql");
    runSql(dataSource, "schema.sql");
  }

  public static GitInfo newGitInfo(int repositoryId, String repositoryName, String branch) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, repositoryId, branch, true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
