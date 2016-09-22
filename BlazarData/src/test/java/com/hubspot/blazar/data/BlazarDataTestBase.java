package com.hubspot.blazar.data;

import org.junit.After;

import com.google.inject.Inject;
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
}
