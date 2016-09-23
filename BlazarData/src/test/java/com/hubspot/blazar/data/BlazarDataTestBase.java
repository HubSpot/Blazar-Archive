package com.hubspot.blazar.data;

import org.junit.Before;

import com.hubspot.blazar.test.base.service.BlazarTestBase;

import io.dropwizard.db.ManagedDataSource;

public class BlazarDataTestBase extends BlazarTestBase {

  @Before
  public void beforeTestBase(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "schema.sql");
  }
}
