package com.hubspot.blazar.guice;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.blazar.listener.SingularityTaskKiller;
import com.hubspot.blazar.util.ManagedScheduledExecutorServiceProvider;
import com.hubspot.blazar.util.SingularityBuildWatcher;
import com.hubspot.singularity.client.SingularityClientModule;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import java.util.concurrent.ScheduledExecutorService;

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

    Multibinder.newSetBinder(binder, LeaderLatchListener.class).addBinding().to(SingularityBuildWatcher.class);
    binder.bind(ScheduledExecutorService.class)
        .annotatedWith(Names.named("SingularityBuildWatcher"))
        .toProvider(new ManagedScheduledExecutorServiceProvider(1, "SingularityBuildWatcher"))
        .in(Scopes.SINGLETON);

    binder.bind(SingularityTaskKiller.class);
  }
}
