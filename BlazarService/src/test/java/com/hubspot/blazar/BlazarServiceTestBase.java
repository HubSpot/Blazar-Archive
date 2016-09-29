package com.hubspot.blazar;

import static org.assertj.core.api.Assertions.fail;

import org.junit.After;

import com.hubspot.blazar.test.base.service.DatabaseBackedTest;

public class BlazarServiceTestBase extends DatabaseBackedTest {

  @After
  public void checkEventBusExceptions() throws Exception {
    if (BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size() > 0) {
      fail(String.format("Event bus exception count was %d (> 0), check log for stack traces.", BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size()));
    }
  }
}
