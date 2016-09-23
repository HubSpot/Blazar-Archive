package com.hubspot.blazar.data;

import org.junit.Before;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.test.base.service.BlazarTestBase;

import io.dropwizard.db.ManagedDataSource;

public class BlazarDataTestBase extends BlazarTestBase {

  @Before
  public void beforeTestBase(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "schema.sql");
  }

  public static GitInfo newGitInfo(int repositoryId, String repositoryName, String branch) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, repositoryId, branch, true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
