package com.hubspot.blazar.guice;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestFilter;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.GitHubNamingFilter;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarConfigurationWrapper;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.exception.IllegalArgumentExceptionMapper;
import com.hubspot.blazar.exception.IllegalStateExceptionMapper;
import com.hubspot.blazar.listener.BuildVisitorModule;
import com.hubspot.blazar.resources.BranchResource;
import com.hubspot.blazar.resources.BranchStateResource;
import com.hubspot.blazar.resources.BuildHistoryResource;
import com.hubspot.blazar.resources.GitHubWebhookResource;
import com.hubspot.blazar.resources.InstantMessageResource;
import com.hubspot.blazar.resources.InterProjectBuildResource;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.blazar.resources.RepositoryBuildResource;
import com.hubspot.blazar.util.GitHubWebhookHandler;
import com.hubspot.blazar.util.LostBuildCleaner;
import com.hubspot.blazar.util.ManagedScheduledExecutorServiceProvider;
import com.hubspot.dropwizard.guicier.DropwizardAwareModule;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.RetryStrategy;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import com.hubspot.horizon.ning.NingHttpClient;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;
import com.hubspot.singularity.client.SingularityClient;
import com.hubspot.singularity.client.SingularityClientModule;

import io.dropwizard.db.DataSourceFactory;

public class BlazarServiceModule extends DropwizardAwareModule<BlazarConfigurationWrapper> {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarServiceModule.class);

  /**
   * Blazar has an option to enable "webhook only" mode. This is because you may wish to have
   * a public facing Blazar instance that does not have the ability to start builds etc. But can still
   * receive and process webhooks from external GitHub installations (like GitHub.Com).
   *
   * In webhook only mode we configure only the bare minimum required resources for Blazar to be able
   * to accept web-hook events and send them into our SQL backed event bus. Other Blazar instances running
   * against the same database are then able to process those events using the SQL backed event bus. These
   * instances are also configured to accept webhooks, but also have the rest of Blazar's API enabled.
   */
  @Override
  public void configure(Binder binder) {
    BlazarConfiguration blazarConfiguration = getConfiguration().getBlazarConfiguration();
    configureWebhooks(binder, blazarConfiguration);

    // Stop here for webhook instances so that they only have the Webhook API enabled.
    if (blazarConfiguration.isWebhookOnly()) {
      return;
    }

    configureRemaining(binder, blazarConfiguration);
  }

  private void configureWebhooks(Binder binder, BlazarConfiguration blazarConfiguration) {
    // Bind GitHub configurations
    MapBinder<String, GitHub> mapBinder = MapBinder.newMapBinder(binder, String.class, GitHub.class);
    for (Map.Entry<String, GitHubConfiguration> entry : blazarConfiguration.getGitHubConfiguration().entrySet()) {
      String host = entry.getKey();
      mapBinder.addBinding(host).toInstance(toGitHub(host, entry.getValue()));
    }

    binder.install(new BlazarEventBusModule());
    binder.install(new BlazarDataModule());

    binder.bind(DataSourceFactory.class).toInstance(blazarConfiguration.getDatabaseConfiguration());
    binder.bind(MetricRegistry.class).toInstance(getEnvironment().metrics());
    binder.bind(ObjectMapper.class).toInstance(getEnvironment().getObjectMapper());
    binder.bind(IllegalArgumentExceptionMapper.class);
    binder.bind(IllegalStateExceptionMapper.class);
    Multibinder.newSetBinder(binder, ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);
    // the webhook resource that lets you post webhooks
    binder.bind(GitHubWebhookResource.class);
  }

  private void configureRemaining(Binder binder, BlazarConfiguration blazarConfiguration) {
    binder.install(new DiscoveryModule());
    binder.install(new BlazarSlackModule(blazarConfiguration));
    binder.bind(PropertyFilteringMessageBodyWriter.class)
        .toConstructor(defaultConstructor(PropertyFilteringMessageBodyWriter.class))
        .in(Scopes.SINGLETON);
    binder.bind(YAMLFactory.class).toInstance(new YAMLFactory());
    binder.bind(XmlFactory.class).toInstance(new XmlFactory());

    // Bind resources
    binder.bind(BranchResource.class);
    binder.bind(BranchStateResource.class);
    binder.bind(ModuleBuildResource.class);
    binder.bind(RepositoryBuildResource.class);
    binder.bind(BuildHistoryResource.class);
    binder.bind(InstantMessageResource.class);
    binder.bind(InterProjectBuildResource.class);

    // Only configure leader-based activities like processing events etc. if you are connected to zookeeper
    if (blazarConfiguration.getZooKeeperConfiguration().isPresent()) {
      binder.bind(GitHubWebhookHandler.class); // Event processing for GitHub webhook events.
      binder.install(new BuildVisitorModule()); // Configures event processors for all build events.
      binder.install(new BlazarQueueProcessorModule());
      binder.install(new BlazarZooKeeperModule());

      Multibinder.newSetBinder(binder, LeaderLatchListener.class).addBinding().to(LostBuildCleaner.class);
      binder.bind(ScheduledExecutorService.class)
        .annotatedWith(Names.named("LostBuildCleaner"))
        .toProvider(new ManagedScheduledExecutorServiceProvider(1, "LostBuildCleaner"))
        .in(Scopes.SINGLETON);

      // Bind and configure Singularity clients for the available clusters
      binder.install(new SingularityClientModule());
      binder.bind(new TypeLiteral<Map<String, SingularityClient>>() {}).toProvider(SingularityClusterClientsProvider.class);
    } else {
      LOG.info("Not enabling queue-processing or build event handlers because no zookeeper configuration is specified. We need to elect a leader to process events.");
    }

  }

  @Provides
  @Singleton
  public BlazarConfiguration getBlazarConfiguration() {
    return getConfiguration().getBlazarConfiguration();
  }

  @Provides
  @Singleton
  @Named("whitelist")
  public Set<String> providesWhitelist() {
    return getConfiguration().getBlazarConfiguration().getWhitelist();
  }

  @Provides
  @Singleton
  @Named("blacklist")
  public Set<String> providesBlacklist() {
    return getConfiguration().getBlazarConfiguration().getBlacklist();
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
  @Singleton
  public HttpClient provideHttpClient(ObjectMapper objectMapper) {
    return new NingHttpClient(HttpConfig.newBuilder().setMaxRetries(5).setObjectMapper(objectMapper).build());
  }

  public static GitHub toGitHub(String host, GitHubConfiguration gitHubConfig) {
    final String endpoint;
    if ("github.com".equals(host) || "api.github.com".equals(host)) {
      endpoint = "https://api.github.com";
    } else {
      endpoint = "https://" + host + "/api/v3";
    }

    GitHubBuilder builder = new GitHubBuilder().withEndpoint(endpoint).withRateLimitHandler(RateLimitHandler.FAIL);

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
