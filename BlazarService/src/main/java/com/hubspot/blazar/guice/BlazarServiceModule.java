package com.hubspot.blazar.guice;

import java.io.IOException;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.hubspot.blazar.discovery.BlazarConfigModuleDiscovery;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import com.hubspot.blazar.resources.BranchResource;
import com.hubspot.blazar.resources.BuildHistoryResource;
import com.hubspot.blazar.resources.BuildResource;
import com.hubspot.blazar.resources.GitHubWebhookResource;
import com.hubspot.blazar.resources.IndexResource;
import com.hubspot.blazar.util.BlazarServiceLoader;
import com.hubspot.blazar.util.BuildLauncher;
import com.hubspot.blazar.discovery.CompositeModuleDiscovery;
import com.hubspot.blazar.util.GitHubWebhookHandler;
import com.hubspot.blazar.util.LoggingHandler;
import com.hubspot.blazar.discovery.maven.MavenModuleDiscovery;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.RetryStrategy;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;

public class BlazarServiceModule extends ConfigurationAwareModule<BlazarConfiguration> {

  @Override
  protected void configure(Binder binder, BlazarConfiguration configuration) {
    binder.install(new BlazarDataModule());
    binder.install(new BlazarZooKeeperModule());

    binder.bind(PropertyFilteringMessageBodyWriter.class).in(Scopes.SINGLETON);

    binder.bind(GitHubWebhookResource.class);
    if (!configuration.isWebhookOnly()) {
      binder.bind(IndexResource.class);
      binder.bind(BranchResource.class);
      binder.bind(BuildResource.class);
      binder.bind(BuildHistoryResource.class);
    }

    binder.bind(GitHubWebhookHandler.class);
    binder.bind(LoggingHandler.class);
    binder.bind(BuildLauncher.class);

    Multibinder.newSetBinder(binder, ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);

    Multibinder<ModuleDiscovery> multibinder = Multibinder.newSetBinder(binder, ModuleDiscovery.class);
    multibinder.addBinding().to(BlazarConfigModuleDiscovery.class);
    multibinder.addBinding().to(MavenModuleDiscovery.class);
    for (Class<? extends ModuleDiscovery> moduleDiscovery : BlazarServiceLoader.load(ModuleDiscovery.class)) {
      multibinder.addBinding().to(moduleDiscovery);
    }

    binder.bind(ModuleDiscovery.class).to(CompositeModuleDiscovery.class);

    MapBinder<String, GitHub> mapBinder = MapBinder.newMapBinder(binder, String.class, GitHub.class);
    for (Entry<String, GitHubConfiguration> entry : configuration.getGitHubConfiguration().entrySet()) {
      String host = entry.getKey();
      mapBinder.addBinding(host).toInstance(toGitHub(host, entry.getValue()));
    }
  }

  @Provides
  @Singleton
  public DataSourceFactory providesDataSourceFactory(BlazarConfiguration configuration) {
    return configuration.getDatabaseConfiguration();
  }

  @Provides
  @Singleton
  public YAMLFactory providesYAMLFactory() {
    return new YAMLFactory();
  }

  @Provides
  @Singleton
  public XmlFactory providesXmlFactory() {
    return new XmlFactory();
  }

  @Provides
  @Singleton
  public ObjectMapper providesObjectMapper(Environment environment) {
    return environment.getObjectMapper();
  }

  @Provides
  @Singleton
  public AsyncHttpClient providesAsyncHttpClient(ObjectMapper mapper) {
    HttpConfig config = HttpConfig.newBuilder().setObjectMapper(mapper).setRetryStrategy(new RetryStrategy() {

      @Override
      public boolean shouldRetry(@Nonnull HttpRequest request, @Nonnull HttpResponse response) {
        return response.getStatusCode() == 409 || RetryStrategy.DEFAULT.shouldRetry(request, response);
      }

      @Override
      public boolean shouldRetry(@Nonnull HttpRequest request, @Nonnull IOException exception) {
        return RetryStrategy.DEFAULT.shouldRetry(request, exception);
      }
    }).build();

    return new NingAsyncHttpClient(config);
  }

  public static GitHub toGitHub(String host, GitHubConfiguration gitHubConfig) {
    final String endpoint;
    if ("github.com".equals(host) || "api.github.com".equals(host)) {
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
