package com.hubspot.blazar.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.GitHubNamingFilter;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.resources.BuildResource;
import com.hubspot.blazar.resources.GitHubWebhookResource;
import com.hubspot.blazar.util.BuildLauncher;
import com.hubspot.blazar.util.GitHubWebhookHandler;
import com.hubspot.blazar.util.LoggingHandler;
import com.hubspot.blazar.util.ModuleDiscovery;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

public class BlazarServiceModule extends ConfigurationAwareModule<BlazarConfiguration> {

  @Override
  protected void configure(Binder binder, BlazarConfiguration configuration) {
    binder.install(new BlazarDataModule());

    binder.bind(PropertyFilteringMessageBodyWriter.class).in(Scopes.SINGLETON);

    binder.bind(BuildResource.class);
    binder.bind(GitHubWebhookResource.class);

    binder.bind(ModuleDiscovery.class);
    binder.bind(GitHubWebhookHandler.class);
    binder.bind(LoggingHandler.class);
    binder.bind(BuildLauncher.class);

    Multibinder.newSetBinder(binder, ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);

    MapBinder<String, GitHub> mapBinder = MapBinder.newMapBinder(binder, String.class, GitHub.class);
    for (Entry<String, GitHubConfiguration> entry : configuration.getGitHubConfiguration().entrySet()) {
      String host = entry.getKey();
      mapBinder.addBinding(host).toInstance(toGitHub(host, entry.getValue()));
    }
  }

  @Provides
  @Singleton
  public EventBus providesEventBus(Environment environment) {
    Executor executor = environment.lifecycle()
        .executorService("GitHubEventProcessor-%d")
        .shutdownTime(Duration.seconds(120))
        .build();

    return new AsyncEventBus("GitHubEventProcessor", executor);
  }

  @Provides
  @Singleton
  public DataSourceFactory providesDataSourceFactory(BlazarConfiguration configuration) {
    return configuration.getDatabaseConfiguration();
  }

  @Provides
  @Singleton
  public XmlMapper providesXmlMapper() {
    return new XmlMapper();
  }

  @Provides
  @Singleton
  public ObjectMapper providesObjectMapper(Environment environment) {
    return environment.getObjectMapper();
  }

  @Provides
  @Singleton
  public AsyncHttpClient providesAsyncHttpClient(ObjectMapper mapper) {
    return new NingAsyncHttpClient(HttpConfig.newBuilder().setObjectMapper(mapper).build());
  }

  public static GitHub toGitHub(String host, GitHubConfiguration gitHubConfig) {
    final String endpoint;
    if ("api.github.com".equals(host)) {
      endpoint = "https://api.github.com";
    } else {
      endpoint = "https://" + host + "/api/v3";
    }

    GitHubBuilder builder = new GitHubBuilder().withEndpoint(endpoint);

    if (gitHubConfig.getOauthToken().isPresent()) {
      builder.withOAuthToken(gitHubConfig.getOauthToken().get(), gitHubConfig.getUser().orNull());
    } else if (gitHubConfig.getPassword().isPresent()) {
      builder.withPassword(gitHubConfig.getUser().orNull(), gitHubConfig.getPassword().get());
    }

    try {
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
