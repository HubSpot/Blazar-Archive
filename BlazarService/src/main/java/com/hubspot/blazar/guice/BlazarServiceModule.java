package com.hubspot.blazar.guice;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.GitHubNamingFilter;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.integration.slack.SlackClient;
import com.hubspot.blazar.listener.BuildVisitorModule;
import com.hubspot.blazar.resources.BranchResource;
import com.hubspot.blazar.resources.BranchStateResource;
import com.hubspot.blazar.resources.BuildHistoryResource;
import com.hubspot.blazar.resources.FeedbackResource;
import com.hubspot.blazar.resources.GitHubWebhookResource;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.blazar.resources.RepositoryBuildResource;
import com.hubspot.blazar.resources.InstantMessageResource;
import com.hubspot.blazar.util.BlazarUrlHelper;
import com.hubspot.blazar.util.GitHubHelper;
import com.hubspot.blazar.util.GitHubWebhookHandler;
import com.hubspot.blazar.util.LoggingHandler;
import com.hubspot.blazar.util.ModuleBuildLauncher;
import com.hubspot.blazar.util.RepositoryBuildLauncher;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.RetryStrategy;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import com.hubspot.horizon.ning.NingHttpClient;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;

public class BlazarServiceModule extends ConfigurationAwareModule<BlazarConfiguration> {

  @Override
  protected void configure(Binder binder, BlazarConfiguration configuration) {
    binder.install(new BlazarZooKeeperModule());
    binder.bind(GitHubWebhookResource.class);
    Multibinder.newSetBinder(binder, ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);

    if (configuration.isWebhookOnly()) {
      return;
    }

    binder.install(new BlazarDataModule());
    binder.install(new BlazarSingularityModule());
    binder.install(new BuildVisitorModule());
    binder.install(new DiscoveryModule());

    binder.bind(BranchResource.class);
    binder.bind(BranchStateResource.class);
    binder.bind(ModuleBuildResource.class);
    binder.bind(RepositoryBuildResource.class);
    binder.bind(BuildHistoryResource.class);
    binder.bind(InstantMessageResource.class);
    binder.bind(FeedbackResource.class);


    binder.bind(DataSourceFactory.class).toInstance(configuration.getDatabaseConfiguration());
    binder.bind(PropertyFilteringMessageBodyWriter.class)
        .toConstructor(defaultConstructor(PropertyFilteringMessageBodyWriter.class))
        .in(Scopes.SINGLETON);

    binder.bind(GitHubWebhookHandler.class);
    binder.bind(LoggingHandler.class);
    binder.bind(GitHubHelper.class);
    binder.bind(RepositoryBuildLauncher.class);
    binder.bind(ModuleBuildLauncher.class);
    binder.bind(SingularityBuildLauncher.class);
    binder.bind(BlazarUrlHelper.class);

    MapBinder<String, GitHub> mapBinder = MapBinder.newMapBinder(binder, String.class, GitHub.class);
    for (Entry<String, GitHubConfiguration> entry : configuration.getGitHubConfiguration().entrySet()) {
      String host = entry.getKey();
      mapBinder.addBinding(host).toInstance(toGitHub(host, entry.getValue()));
    }
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
  public MetricRegistry providesMetricRegistry(Environment environment) {
    return environment.metrics();
  }

  @Provides
  @Singleton
  public ObjectMapper providesObjectMapper(Environment environment) {
    return environment.getObjectMapper();
  }

  @Provides
  @Singleton
  @Named("whitelist")
  public Set<String> providesWhitelist(BlazarConfiguration configuration) {
    return configuration.getWhitelist();
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


  @Provides
  @com.google.inject.Singleton
  public HttpClient provideHttpClient(ObjectMapper objectMapper) {
    return new NingHttpClient(HttpConfig.newBuilder().setMaxRetries(5).setObjectMapper(objectMapper).build());
  }

  @Provides
  @Singleton
  public SlackClient providesSlackClient(AsyncHttpClient asyncHttpClient, BlazarConfiguration blazarConfiguration, ObjectMapper objectMapper, HttpClient httpClient) {
    return new SlackClient(asyncHttpClient, blazarConfiguration, objectMapper, httpClient);
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

  private static <T> Constructor<T> defaultConstructor(Class<T> type) {
    try {
      return type.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
