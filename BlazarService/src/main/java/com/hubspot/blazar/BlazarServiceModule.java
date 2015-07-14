package com.hubspot.blazar;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;

public class BlazarServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PropertyFilteringMessageBodyWriter.class).in(Scopes.SINGLETON);

    bind(TestResource.class);
  }
}
