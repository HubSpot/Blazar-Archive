package com.hubspot.blazar.guice;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.singularity.client.SingularityClientModule;

public class BlazarSingularityModule extends ConfigurationAwareModule<BlazarConfiguration> {

  @Override
  protected void configure(Binder binder, BlazarConfiguration configuration) {
    SingularityConfiguration singularityConfiguration = configuration.getSingularityConfiguration();

    binder.install(new SingularityClientModule(ImmutableList.of(singularityConfiguration.getHost())));
    if (singularityConfiguration.getPath().isPresent()) {
      SingularityClientModule.bindContextPath(binder).toInstance(singularityConfiguration.getPath().get());
    }
    if (singularityConfiguration.getCredentials().isPresent()) {
      SingularityClientModule.bindCredentials(binder).toInstance(singularityConfiguration.getCredentials().get());
    }
  }
}
