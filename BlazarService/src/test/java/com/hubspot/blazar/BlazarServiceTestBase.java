package com.hubspot.blazar;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;

import com.google.inject.Inject;
import com.hubspot.blazar.listener.BuildEventDispatcher;
import com.hubspot.blazar.queue.QueueProcessor;
import com.hubspot.blazar.test.base.service.DatabaseBackedTest;

public class BlazarServiceTestBase extends DatabaseBackedTest {

  @Inject
  QueueProcessor queueProcessor;
  @Inject
  BuildEventDispatcher buildEventDispatcher;

  @Before
  public void startEventBus() {
    queueProcessor.isLeader();
    queueProcessor.startProcessorWithCustomPollingRate(50, TimeUnit.MILLISECONDS);
  }

  @After
  public void stopEventBus() {
    queueProcessor.notLeader();
    queueProcessor.stop();
  }

  @After
  public void checkEventBusExceptions() throws Exception {
    if (BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size() > 0) {
      fail(String.format("Event bus exception count was %d (> 0), check log for stack traces.", BlazarServiceTestModule.EVENT_BUS_EXCEPTION_COUNT.size()));
    }
  }
}
