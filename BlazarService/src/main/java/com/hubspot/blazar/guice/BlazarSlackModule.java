package com.hubspot.blazar.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.visitor.repositorybuild.SlackDmNotificationVisitor;
import com.hubspot.blazar.visitor.repositorybuild.SlackRoomNotificationVisitor;
import com.hubspot.blazar.resources.SlackResource;
import com.hubspot.blazar.resources.UserFeedbackResource;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

/**
 * This module handles the configuration of all slack-related features inside of Blazar.
 * This module does not bind any slack related resources / clients / visitors etc. if slack is not configured.
 */
public class BlazarSlackModule implements Module {

  private static final Logger LOG = LoggerFactory.getLogger(BlazarSlackModule.class);
  private final Optional<BlazarSlackConfiguration> slackConfiguration;

  public BlazarSlackModule(BlazarConfiguration configuration) {
    this.slackConfiguration = configuration.getSlackConfiguration();
  }

  @Override
  public void configure(Binder binder) {

    if (!slackConfiguration.isPresent()) {
      LOG.info("Slack is not configured, not binding slack related resources or slack build notification visitors");
      return;
    }

    Multibinder<RepositoryBuildVisitor> repositoryBuildVisitorMultibinder = Multibinder.newSetBinder(binder, RepositoryBuildVisitor.class);
    repositoryBuildVisitorMultibinder.addBinding().to(SlackDmNotificationVisitor.class);
    repositoryBuildVisitorMultibinder.addBinding().to(SlackRoomNotificationVisitor.class);

    binder.bind(SlackSession.class).toInstance(SlackSessionFactory.createWebSocketSlackSession(slackConfiguration.get().getSlackApiToken()));
    binder.bind(SlackResource.class);
    binder.bind(UserFeedbackResource.class);
  }
}
