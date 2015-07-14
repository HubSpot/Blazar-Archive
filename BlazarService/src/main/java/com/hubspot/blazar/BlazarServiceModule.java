package com.hubspot.blazar;

import com.google.inject.AbstractModule;

public class BlazarServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TestResource.class);
  }
}
