package com.hubspot.blazar.guice;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.queue.QueueProcessor;
import com.hubspot.blazar.util.ManagedScheduledExecutorServiceProvider;

public class BlazarQueueProcessorModule implements Module {

  @Override
  public void configure(Binder binder) {
    Multibinder.newSetBinder(binder, LeaderLatchListener.class).addBinding().to(QueueProcessor.class);
    binder.bind(ScheduledExecutorService.class)
        .annotatedWith(Names.named("QueueProcessor"))
        .toProvider(new ManagedScheduledExecutorServiceProvider(1, "QueueProcessor"))
        .in(Scopes.SINGLETON);
  }
}
