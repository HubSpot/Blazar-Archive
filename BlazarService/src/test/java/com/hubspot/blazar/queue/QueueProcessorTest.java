package com.hubspot.blazar.queue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.BuildTrigger.Type;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.guice.BlazarEventBusModule;
import com.hubspot.blazar.guice.BlazarQueueProcessorModule;
import com.hubspot.blazar.test.base.service.BlazarTestBase;
import com.hubspot.blazar.test.base.service.BlazarTestModule;

public class QueueProcessorTest extends BlazarTestBase {
  private EventBus eventBus;
  private List<Object> received;

  @Before
  public void setup() throws Exception {
    synchronized (injector) {
      if (injector.get() == null) {
        injector.set(
            Guice.createInjector(
                new BlazarTestModule(),
                new BlazarDataModule(),
                new BlazarEventBusModule(),
                new BlazarQueueProcessorModule()
            )
        );
        runSql("schema.sql");
        getFromGuice(EventBus.class).register(new Object() {

          @Subscribe
          public void handleEvent(Object event) {
            received.add(event);
          }
        });
      }
    }

    eventBus = getFromGuice(EventBus.class);
    received = new ArrayList<>();

    QueueProcessor queueProcessor = getFromGuice(QueueProcessor.class);
    queueProcessor.start();
    queueProcessor.isLeader();
  }

  @After
  public void cleanup() throws Exception {
    QueueProcessor queueProcessor = getFromGuice(QueueProcessor.class);
    queueProcessor.stop();
    queueProcessor.notLeader();

    runSql("schema.sql");
  }

  @Test
  public void itProcessesEvents() {
    BuildTrigger event = new BuildTrigger(Type.PUSH, "abc");
    eventBus.post(event);

    waitForEvent();
    assertThat(received).containsExactly(event);
  }

  @Test
  public void itRetriesFailedEvents() {
    eventBus.register(new Object() {
      AtomicBoolean threw = new AtomicBoolean();

      @Subscribe
      public void handleEvent(Object event) {
        if (threw.compareAndSet(false, true)) {
          throw new RuntimeException();
        }
      }
    });

    BuildTrigger event = new BuildTrigger(Type.PUSH, "abc");
    eventBus.post(event);

    waitForEvent();
    waitForEvent(11200, TimeUnit.MILLISECONDS);
    assertThat(received).containsExactly(event, event);
  }

  @Test
  public void itDoesntDoubleProcessItems() {
    CountDownLatch latch = new CountDownLatch(1);

    eventBus.register(new Object() {
      AtomicBoolean slept = new AtomicBoolean();

      @Subscribe
      public void handleEvent(Object event) throws InterruptedException {
        if (slept.compareAndSet(false, true)) {
          Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
          latch.countDown();
        }
      }
    });

    BuildTrigger event = new BuildTrigger(Type.PUSH, "abc");
    eventBus.post(event);

    Uninterruptibles.awaitUninterruptibly(latch);
    Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
    assertThat(received).containsExactly(event);
  }

  private void waitForEvent() {
    waitForEvent(1200, TimeUnit.MILLISECONDS);
  }

  private void waitForEvent(long amount, TimeUnit unit) {
    int events = received.size();
    long start = System.nanoTime();
    long stop = start + unit.toNanos(amount);

    while (received.size() == events && System.nanoTime() < stop) {
      Uninterruptibles.sleepUninterruptibly(25, TimeUnit.MILLISECONDS);
    }

    if (received.size() == events) {
      throw new RuntimeException(new TimeoutException("No event received"));
    }
  }
}
