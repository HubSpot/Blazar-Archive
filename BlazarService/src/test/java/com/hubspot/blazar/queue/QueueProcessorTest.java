package com.hubspot.blazar.queue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.hubspot.blazar.BlazarServiceTestBase;
import com.hubspot.blazar.BlazarServiceTestModule;
import com.hubspot.blazar.base.BuildMetadata;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class QueueProcessorTest extends BlazarServiceTestBase {
  private List<Object> received = new ArrayList<>();

  @Inject
  private EventBus eventBus;

  @Inject
  private QueueProcessor queueProcessor;

  @Before
  public void before() {
    eventBus.register(new Object() {
      @Subscribe
      public void handleEvent(Object event) {
        received.add(event);
      }
    });
  }


  @Test
  public void itProcessesEvents() {
    BuildMetadata event = BuildMetadata.push("testUser");
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

    BuildMetadata event = BuildMetadata.push("testUser");
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

    BuildMetadata event = BuildMetadata.push("testUser");
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
