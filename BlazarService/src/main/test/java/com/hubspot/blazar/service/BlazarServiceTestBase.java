package com.hubspot.blazar.service;

import org.junit.After;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.hubspot.blazar.test.base.service.BlazarTestBase;

public class BlazarServiceTestBase extends BlazarTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarServiceTestBase.class);

  @BeforeClass
  public static void setup() throws Exception {
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
  }
}
