package com.hubspot.blazar.guice;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.hubspot.singularity.SingularityClientCredentials;
import com.hubspot.singularity.client.SingularityClientModule;

public class BlazarSingularityModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.install(new SingularityClientModule(ImmutableList.of(System.getenv("SINGULARITY_HOST"))));
    SingularityClientModule.bindContextPath(binder).toInstance("singularity/v2/api");
    SingularityClientModule.bindCredentials(binder).toInstance(new SingularityClientCredentials("X-HubSpot-User", "jhaber"));
  }
}
