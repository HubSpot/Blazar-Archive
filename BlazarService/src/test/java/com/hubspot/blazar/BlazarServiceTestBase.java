package com.hubspot.blazar;

import static org.assertj.core.api.Assertions.fail;

import org.junit.After;
import org.junit.Before;

import com.google.inject.Guice;
import com.hubspot.blazar.test.base.service.BlazarTestBase;

public class BlazarServiceTestBase extends BlazarTestBase {

  @Before
  public void setup() throws Exception {
    synchronized (injector) {
      if (injector.get() == null) {
        injector.set(Guice.createInjector(new BlazarServiceTestModule()));
        runSql("schema.sql");
      }
    }
  }

  @After
  public void cleanup() throws Exception {
    runSql("schema.sql");
    if (BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size() > 0) {
      fail(String.format("Event bus exception count was %d (> 0), check log for stack traces.", BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size()));
    }
  }
}
