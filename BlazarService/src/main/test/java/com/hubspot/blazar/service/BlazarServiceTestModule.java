package com.hubspot.blazar.service;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.discovery.DiscoveryModule;
import com.hubspot.blazar.integration.slack.SlackClient;
import com.hubspot.blazar.listener.BuildVisitorModule;
import com.hubspot.blazar.test.base.service.BlazarTestModule;
import com.hubspot.blazar.util.GitHubHelper;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.singularity.client.SingularityClient;

import static org.mockito.Mockito.mock;

public class BlazarServiceTestModule extends AbstractModule {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarServiceTestModule.class);

  @Override
  public void configure() {
    bind(BlazarConfiguration.class).toInstance(mock(BlazarConfiguration.class));
    install(new BlazarTestModule());
    install(new BlazarDataModule());
    install(new BuildVisitorModule());
    install(new DiscoveryModule());
    bind(EventBus.class).toInstance(buildEventBus());
    bind(SingularityClient.class).toInstance(mock(SingularityClient.class));
    bind(SingularityBuildLauncher.class).toInstance(mock(SingularityBuildLauncher.class));
    bind(GHRepository.class).toInstance(mock(GHRepository.class));
    bind(SlackClient.class).toInstance(mock(SlackClient.class));
    bind(GitHubHelper.class).toInstance(mock(GitHubHelper.class));
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
