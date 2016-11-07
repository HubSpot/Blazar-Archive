package com.hubspot.blazar.guice;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.hubspot.blazar.data.BlazarDaoModule;
import com.hubspot.blazar.queue.SqlEventBus;

public class BlazarEventBusModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.install(new BlazarDaoModule());
    binder.bind(SqlEventBus.class);
  }

  @Provides
  @Singleton
  public EventBus providesEventBus(SqlEventBus sqlEventBus) {
    return sqlEventBus;
  }

  @Provides
  @Singleton
  public Set<Object> erroredQueueItems() {
    return Sets.newConcurrentHashSet();
  }
}
