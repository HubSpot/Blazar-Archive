package com.hubspot.blazar.guice;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.listener.SlackDmNotificationVisitor;
import com.hubspot.blazar.listener.SlackRoomNotificationVisitor;
import com.hubspot.blazar.resources.SlackResource;
import com.hubspot.blazar.resources.UserFeedbackResource;
import com.hubspot.blazar.util.SlackClientWrapper;
import com.hubspot.blazar.util.SlackMessageBuildingUtils;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

/**
 * This module handles the configuration of all slack-related features inside of Blazar.
 * This module does not bind any slack related resources / clients / visitors etc. if slack is not configured.
 */
public class BlazarSlackModule implements Module {

  public static final String SLACK_FEEDBACK_ROOM = "SLACK_FEDBACK_ROOM";
  private static final Logger LOG = LoggerFactory.getLogger(BlazarSlackModule.class);
  private final Optional<BlazarSlackConfiguration> slackConfiguration;
  private final Multibinder<RepositoryBuildVisitor> repositoryBuildVisitorMultibinder;

  public BlazarSlackModule(BlazarConfiguration configuration, Multibinder<RepositoryBuildVisitor> repositoryBuildVisitorMultibinder) {
    this.slackConfiguration = configuration.getSlackConfiguration();
    this.repositoryBuildVisitorMultibinder = repositoryBuildVisitorMultibinder;
  }

  @Override
  public void configure(Binder binder) {

    if (!slackConfiguration.isPresent()) {
      LOG.info("Slack is not configured, not binding slack related resources or slack build notification visitors");
      return;
    }

    binder.bind(SlackSession.class).toInstance(makeNewSlackSession());
    binder.bind(SlackMessageBuildingUtils.class);
    repositoryBuildVisitorMultibinder.addBinding().to(SlackDmNotificationVisitor.class);
    repositoryBuildVisitorMultibinder.addBinding().to(SlackRoomNotificationVisitor.class);

    binder.bind(UserFeedbackResource.class);
    binder.bind(SlackClientWrapper.class);
    binder.bind(SlackResource.class);

    if (slackConfiguration.get().getFeedbackRoom().isPresent()) {
      binder.bindConstant().annotatedWith(Names.named(SLACK_FEEDBACK_ROOM)).to(slackConfiguration.get().getFeedbackRoom().get());
      binder.bind(UserFeedbackResource.class);
    } else {
      LOG.info("Not configuring feedback endpoint -- no room configured");
    }

  }

  private SlackSession makeNewSlackSession() {
    try {
      SlackSession session = SlackSessionFactory.createWebSocketSlackSession(slackConfiguration.get().getSlackApiToken());
      session.connect();
      return session;
    } catch (IOException ioe) {
      LOG.error("Error connecting to Slack", ioe);
      throw new RuntimeException("Error connecting to Slack", ioe);
    }
  }
}
