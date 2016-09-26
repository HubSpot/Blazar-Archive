package com.hubspot.blazar.test.base.service;

import org.junit.After;

import com.google.inject.Inject;

import io.dropwizard.db.ManagedDataSource;

public class DatabaseBackedTest extends BlazarTestBase {

  @Inject
  @After
  public void cleanUpData(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "cleanup.sql");
  }
}
