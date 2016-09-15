package com.hubspot.blazar;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.BlazarGHRepository;
import org.kohsuke.github.BlazarGitHub;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.UiConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.externalservice.sentry.DevNullExceptionNotifier;
import com.hubspot.blazar.externalservice.sentry.ExceptionNotifier;
import com.hubspot.blazar.listener.BuildVisitorModule;
import com.hubspot.blazar.listener.GitHubStatusVisitor;
import com.hubspot.blazar.listener.TestBuildLauncher;
import com.hubspot.blazar.test.base.service.BlazarGitTestConfiguration;
import com.hubspot.blazar.test.base.service.BlazarTestModule;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.blazar.util.TestSingularityBuildLauncher;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.singularity.client.SingularityClient;
import com.ullink.slack.simpleslackapi.SlackSession;

public class BlazarServiceTestModule extends AbstractModule {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarServiceTestModule.class);
  public static List<Throwable> EVENT_BUS_EXCEPTION_COUNT = new ArrayList<>();
  @Override
  public void configure() {
    EventBus eventBus = buildEventBus();
    install(new BlazarTestModule());
    install(new BlazarDataModule());
    install(new BuildVisitorModule(buildBlazarConfiguration()));
    install(new DiscoveryModule());
    bind(GitHubStatusVisitor.class).toInstance(mock(GitHubStatusVisitor.class));
    bind(EventBus.class).toInstance(eventBus);
    bind(BlazarConfiguration.class).toInstance(buildBlazarConfiguration());
    bind(SingularityClient.class).toInstance(mock(SingularityClient.class));
    bind(SlackSession.class).toInstance(mock(SlackSession.class));
    bind(AsyncHttpClient.class).toInstance(mock(AsyncHttpClient.class));
    bind(Integer.class).annotatedWith(Names.named("")).toInstance(0);
    bind(ExceptionNotifier.class).to(DevNullExceptionNotifier.class);

    Multibinder<ModuleBuildVisitor> moduleBuildVisitors = Multibinder.newSetBinder(binder(), ModuleBuildVisitor.class);
    moduleBuildVisitors.addBinding().to(TestBuildLauncher.class);
    bindGitHubMap(); // does its own binding
  }

  @Singleton
  @Provides
  public SingularityBuildLauncher providesSingularityBuildLauncher(TestSingularityBuildLauncher testSingularityBuildLauncher) {
    return testSingularityBuildLauncher;
  }

  private BlazarConfiguration buildBlazarConfiguration() {
    return new BlazarConfiguration() {
      private final UiConfiguration uiConfiguration = new UiConfiguration("http://localhost/test/base/url");

      @Override
      public UiConfiguration getUiConfiguration() {
        return uiConfiguration;
      }
    };
  }

  private void bindGitHubMap() {
    MapBinder mapBinder = MapBinder.newMapBinder(binder(), String.class, GitHub.class);
    final BlazarGitTestConfiguration blazarGitTestConfiguration;
    try {
      blazarGitTestConfiguration = new YAMLMapper().readValue(Resources.getResource("GitHubData.yaml").openStream(), BlazarGitTestConfiguration.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (Map.Entry<String, List<BlazarGHRepository>> entry : blazarGitTestConfiguration.getConfig().entrySet()) {
      try {
        BlazarGitHub b = BlazarGitHub.getTestBlazarGitHub(entry.getValue());
        mapBinder.addBinding(entry.getKey()).toInstance(b);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private EventBus buildEventBus() {
    return new EventBus(makeSubscriberExceptionHandler(LOG)) {
      @Override
      public void post(Object event) {
        LOG.info("Got event {}", event);
        super.post(event);
      }
    };
  }

  private static SubscriberExceptionHandler makeSubscriberExceptionHandler(final Logger logger) {
    return new SubscriberExceptionHandler() {
      @Override
      public void handleException(Throwable exception, SubscriberExceptionContext context) {
        logger.error("Got error while processing event", exception);
        EVENT_BUS_EXCEPTION_COUNT.add(exception);
      }
    };
  }
}

