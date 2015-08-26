package com.hubspot.blazar.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.Managed;

import javax.inject.Provider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManagedScheduledExecutorServiceProvider implements Provider<ScheduledExecutorService>, Managed {
  private final ScheduledExecutorService service;
  private final AtomicBoolean stopped;

  public ManagedScheduledExecutorServiceProvider(int threads, String name) {
    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(name + "-pool-%d").setDaemon(true).build();
    this.service = Executors.newScheduledThreadPool(threads, threadFactory);
    this.stopped = new AtomicBoolean();
  }

  @Override
  public void start() {}

  @Override
  public ScheduledExecutorService get() {
    Preconditions.checkState(!stopped.get(), "Already stopped");
    return service;
  }

  @Override
  public void stop() throws InterruptedException {
    if (stopped.compareAndSet(false, true)) {
      service.shutdown();
      service.awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
