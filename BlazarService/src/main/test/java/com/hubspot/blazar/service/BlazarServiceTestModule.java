package com.hubspot.blazar.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.BlazarGHRepository;
import org.kohsuke.github.BlazarGitHub;
import org.kohsuke.github.BlazarGitTestConfiguration;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.UiConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.integration.slack.SlackClient;
import com.hubspot.blazar.listener.BuildVisitorModule;
import com.hubspot.blazar.test.base.service.BlazarTestModule;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.blazar.util.TestSingularityBuildLauncher;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.singularity.client.SingularityClient;

import static org.mockito.Mockito.mock;

public class BlazarServiceTestModule extends AbstractModule {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarServiceTestModule.class);

  @Override
  public void configure() {
    EventBus eventBus = buildEventBus();
    install(new BlazarTestModule());
    install(new BlazarDataModule());
    install(new BuildVisitorModule());
    install(new DiscoveryModule());
    bind(EventBus.class).toInstance(eventBus);
    bind(BlazarConfiguration.class).toInstance(buildBlazarConfiguration());
    bind(SingularityClient.class).toInstance(mock(SingularityClient.class));
    bind(SingularityBuildLauncher.class).to(TestSingularityBuildLauncher.class);
    bind(SlackClient.class).toInstance(mock(SlackClient.class));
    bind(AsyncHttpClient.class).toInstance(mock(AsyncHttpClient.class));
    bindGitHubMap(); // does its own binding
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
    return new EventBus() {
      @Override
      public void post(Object event) {
        super.post(event);
        LOG.info("Got event {}", event);
      }
    };
  }
}
