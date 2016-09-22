package com.hubspot.blazar;

import static org.assertj.core.api.Assertions.fail;

import org.junit.After;

import com.google.inject.Inject;
import com.hubspot.blazar.test.base.service.BlazarTestBase;

import io.dropwizard.db.ManagedDataSource;

public class BlazarServiceTestBase extends BlazarTestBase {
  @Inject
  private ManagedDataSource managedDataSource;

  @After
  @Inject
  public void cleanup(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "cleanup.sql");
    if (BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size() > 0) {
      fail(String.format("Event bus exception count was %d (> 0), check log for stack traces.", BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size()));
    }
  }
}
