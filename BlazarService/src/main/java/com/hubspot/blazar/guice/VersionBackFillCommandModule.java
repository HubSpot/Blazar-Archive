package com.hubspot.blazar.guice;

import java.util.Map;

import net.sourceforge.argparse4j.inf.Namespace;

import org.kohsuke.github.GitHub;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.util.GitHubHelper;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;

public class VersionBackFillCommandModule extends AbstractModule {

  private Namespace namespace;
  private BlazarConfiguration configuration;
  private Bootstrap<BlazarConfiguration> bootstrap;

  public VersionBackFillCommandModule(Bootstrap<BlazarConfiguration> bootstrap,
                                      Namespace namespace,
                                      BlazarConfiguration configuration) {
    this.namespace = namespace;
    this.configuration = configuration;
    this.bootstrap = bootstrap;
  }

  @Override
  protected void configure() {
    binder().install(new BlazarDataModule());
    binder().install(new DiscoveryModule());
    binder().bind(GitHubHelper.class);
    binder().bind(DataSourceFactory.class).toInstance(configuration.getDatabaseConfiguration());
    bootstrap.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    bootstrap.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    MapBinder<String, GitHub> mapBinder = MapBinder.newMapBinder(binder(), String.class, GitHub.class);
    for (Map.Entry<String, GitHubConfiguration> entry : configuration.getGitHubConfiguration().entrySet()) {
      String host = entry.getKey();
      mapBinder.addBinding(host).toInstance(BlazarServiceModule.toGitHub(host, entry.getValue()));
    }
  }
}
