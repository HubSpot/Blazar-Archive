package com.hubspot.blazar.test.base.service;

import org.junit.After;
import org.junit.Before;

import com.google.inject.Inject;

import io.dropwizard.db.ManagedDataSource;

public class DatabaseBackedTest extends BlazarTestBase {
  @Inject
  @Before
  public void runSchema(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "schema.sql");
  }

  @Inject
  @After
  public void cleanUpData(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "schema.sql");
    runSql(dataSource, "cleanup.sql");
  }
}
