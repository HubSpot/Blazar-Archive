package com.hubspot.blazar.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.hubspot.blazar.test.base.service.BlazarTestModule;

public class BlazarDataTestModule implements Module {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarDataTestModule.class);

  @Override
  public void configure(Binder binder) {
    binder.bind(EventBus.class).toInstance(buildEventBus());
    binder.install(new BlazarTestModule());
    binder.install(new BlazarDataModule());
  }

  private EventBus buildEventBus() {
    return new EventBus() {

      @Override
      public void post(Object event) {
        LOG.debug("Got event {}", event);
      }
    };
  }

}
