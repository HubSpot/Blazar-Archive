package com.hubspot.blazar.data;

import org.junit.After;
import org.junit.BeforeClass;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.test.base.service.BlazarTestBase;

public class BlazarDataTestBase extends BlazarTestBase {

  @BeforeClass
  public static void setup() throws Exception {
    synchronized (injector) {
      if (injector.get() == null) {
        injector.set(Guice.createInjector(new BlazarDataTestModule()));
        runSql("schema.sql");
      }
    }
  }

  @After
  public void cleanup() throws Exception {
    runSql("cleanup.sql");
    runSql("schema.sql");
  }

  public static GitInfo newGitInfo(int repositoryId, String repositoryName, String branch) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, repositoryId, branch, true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
