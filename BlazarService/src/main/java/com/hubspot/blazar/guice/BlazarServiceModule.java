package com.hubspot.blazar.guice;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map.Entry;
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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.GitHubNamingFilter;
import com.hubspot.blazar.cctray.CCTrayProjectFactory;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.exception.IllegalArgumentExceptionMapper;
import com.hubspot.blazar.exception.IllegalStateExceptionMapper;
import com.hubspot.blazar.listener.BuildVisitorModule;
import com.hubspot.blazar.listener.SingularityTaskKiller;
import com.hubspot.blazar.resources.BranchResource;
import com.hubspot.blazar.resources.BranchStateResource;
import com.hubspot.blazar.resources.BuildHistoryResource;
import com.hubspot.blazar.resources.GitHubWebhookResource;
import com.hubspot.blazar.resources.InstantMessageResource;
import com.hubspot.blazar.resources.InterProjectBuildResource;
import com.hubspot.blazar.resources.ModuleBuildResource;
import com.hubspot.blazar.resources.RepositoryBuildResource;
import com.hubspot.blazar.resources.UiResource;
import com.hubspot.blazar.resources.UserFeedbackResource;
import com.hubspot.blazar.util.BlazarUrlHelper;
import com.hubspot.blazar.util.GitHubHelper;
import com.hubspot.blazar.util.GitHubWebhookHandler;
import com.hubspot.blazar.util.LoggingHandler;
import com.hubspot.blazar.util.ManagedScheduledExecutorServiceProvider;
import com.hubspot.blazar.util.ModuleBuildLauncher;
import com.hubspot.blazar.util.RepositoryBuildLauncher;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.blazar.util.SingularityBuildWatcher;
import com.hubspot.blazar.util.SlackUtils;
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
import com.hubspot.singularity.client.SingularityClientModule;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import io.dropwizard.db.DataSourceFactory;

public class BlazarServiceModule extends DropwizardAwareModule<BlazarConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarServiceModule.class);

  @Override
  public void configure(Binder binder) {
    binder.bind(DataSourceFactory.class).toInstance(getConfiguration().getDatabaseConfiguration());
    binder.install(new BlazarEventBusModule());
    binder.bind(GitHubWebhookResource.class);
    binder.bind(YAMLFactory.class).toInstance(new YAMLFactory());
    binder.bind(XmlFactory.class).toInstance(new XmlFactory());
    binder.bind(MetricRegistry.class).toInstance(getEnvironment().metrics());
    binder.bind(ObjectMapper.class).toInstance(getEnvironment().getObjectMapper());
    Multibinder.newSetBinder(binder, ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);

    if (getConfiguration().isWebhookOnly()) {
      return;
    }

    binder.install(new BlazarDataModule());
    binder.install(new DiscoveryModule());

    binder.bind(IllegalArgumentExceptionMapper.class);
    binder.bind(IllegalStateExceptionMapper.class);

    // Resources
    binder.bind(BranchResource.class);
    binder.bind(BranchStateResource.class);
    binder.bind(ModuleBuildResource.class);
    binder.bind(RepositoryBuildResource.class);
    binder.bind(BuildHistoryResource.class);
    binder.bind(InstantMessageResource.class);
    binder.bind(InterProjectBuildResource.class);
    binder.bind(UiResource.class);

    if (getConfiguration().getSlackConfiguration().isPresent()) {
      binder.bind(UserFeedbackResource.class);
      binder.bind(SlackUtils.class);
    }

    // Only configure leader-based activities like processing events etc. if you are connected to zookeeper
    if (getConfiguration().getZooKeeperConfiguration().isPresent()) {
      binder.install(new BuildVisitorModule(getConfiguration()));
      binder.install(new BlazarQueueProcessorModule());
      binder.install(new BlazarZooKeeperModule());


      // Bind Singularity related watchers
      Multibinder.newSetBinder(binder, LeaderLatchListener.class).addBinding().to(SingularityBuildWatcher.class);
      binder.bind(ScheduledExecutorService.class)
        .annotatedWith(Names.named("SingularityBuildWatcher"))
        .toProvider(new ManagedScheduledExecutorServiceProvider(1, "SingularityBuildWatcher"))
        .in(Scopes.SINGLETON);
      binder.bind(SingularityTaskKiller.class);
    } else {
      LOG.info("Not enabling queue-processing or build event handlers because no zookeeper configuration is specified. We need to elect a leader to process events.");
    }

    // Set up property filtering
    binder.bind(PropertyFilteringMessageBodyWriter.class)
        .toConstructor(defaultConstructor(PropertyFilteringMessageBodyWriter.class))
        .in(Scopes.SINGLETON);

    // Bind various Service level classes
    binder.bind(GitHubWebhookHandler.class);
    binder.bind(LoggingHandler.class);
    binder.bind(GitHubHelper.class);
    binder.bind(RepositoryBuildLauncher.class);
    binder.bind(ModuleBuildLauncher.class);
    binder.bind(SingularityBuildLauncher.class);
    binder.bind(BlazarUrlHelper.class);
    binder.bind(CCTrayProjectFactory.class);

    // Bind and configure Singularity client
    SingularityConfiguration singularityConfiguration = getConfiguration().getSingularityConfiguration();
    binder.install(new SingularityClientModule(ImmutableList.of(singularityConfiguration.getHost())));
    if (singularityConfiguration.getPath().isPresent()) {
      SingularityClientModule.bindContextPath(binder).toInstance(singularityConfiguration.getPath().get());
    }
    if (singularityConfiguration.getCredentials().isPresent()) {
      SingularityClientModule.bindCredentials(binder).toInstance(singularityConfiguration.getCredentials().get());
    }

    // Bind GitHub configurations
    MapBinder<String, GitHub> mapBinder = MapBinder.newMapBinder(binder, String.class, GitHub.class);
    for (Entry<String, GitHubConfiguration> entry : getConfiguration().getGitHubConfiguration().entrySet()) {
      String host = entry.getKey();
      mapBinder.addBinding(host).toInstance(toGitHub(host, entry.getValue()));
    }
  }

  @Provides
  @Singleton
  public Optional<SlackSession> get(BlazarConfiguration configuration) throws IOException {
    if (configuration.getSlackConfiguration().isPresent() && !configuration.isWebhookOnly()) {
      String token = configuration.getSlackConfiguration().get().getSlackApiToken();
      SlackSession session = SlackSessionFactory.createWebSocketSlackSession(token);
      session.connect();
      return Optional.of(session);
    } else {
      return Optional.absent();
    }
  }

  @Provides
  @Singleton
  @Named("whitelist")
  public Set<String> providesWhitelist(BlazarConfiguration configuration) {
    return configuration.getWhitelist();
  }

  @Provides
  @Singleton
  @Named("blacklist")
  public Set<String> providesBlacklist(BlazarConfiguration configuration) {
    return configuration.getBlacklist();
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
