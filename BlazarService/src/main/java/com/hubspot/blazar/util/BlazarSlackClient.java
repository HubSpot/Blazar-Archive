package com.hubspot.blazar.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.replies.ParsedSlackReply;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

/**
 * This class handles all the details of sending a slack message, and verifying that it did send.
 */
@Singleton
public class BlazarSlackClient {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarSlackClient.class);
  private static final Retryer<Boolean> RETRYER = RetryerBuilder.<Boolean>newBuilder()
        .retryIfResult(Predicates.equalTo(Boolean.FALSE))
        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
        .build();

  private SlackSession session;
  private MetricRegistry metricRegistry;

  @Inject
  public BlazarSlackClient(SlackSession session, MetricRegistry metricRegistry) {
    this.session = session;
    this.metricRegistry = metricRegistry;
  }

  public Set<com.hubspot.blazar.externalservice.slack.SlackChannel> getChannels() {
    if (!ensureConnected()) {
      LOG.warn("Unable to get list of slack channels because our slack session is not connected");
      return Collections.emptySet();
    }
    Collection<SlackChannel> channels = session.getChannels();
    return channels.stream().map(channel -> new com.hubspot.blazar.externalservice.slack.SlackChannel(channel.getId(), channel.getName())).collect(Collectors.toSet());
  }

  public void sendMessageToChannel(String channelName, String message, SlackAttachment attachment) {
    Optional<SlackChannel> slackChannel = Optional.fromNullable(session.findChannelByName(channelName));
    if (!slackChannel.isPresent()) {
      LOG.warn("No slack channel found for name {}", channelName);
      return;
    }

    sendMessageToChannel(slackChannel.get(), message, attachment);
  }

  public void sendMessageToChannel(SlackChannel channel, String message, SlackAttachment attachment) {
    try {
      RETRYER.call(() -> sendMessage(channel, message, attachment));
      metricRegistry.counter("successful-slack-channel-sends").inc();
      // Here we swallow exceptions that might be thrown by our retryer or #sendMessage().
      // We don't catch and retry any exceptions because the slack session doesn't throw us any, (it swallows them and returns null)
    } catch (Exception e) {
      metricRegistry.counter("failed-slack-channel-sends").inc();
      LOG.error("Could not send slack message {}", attachment, e);
    }
  }

  public void sendMessageToUser(String email, String message, SlackAttachment attachment) {
    Optional<SlackUser> user = Optional.fromNullable(session.findUserByEmail(email));
    if (!user.isPresent()) {
      LOG.warn("Could not find user with email {} ", email);
      return;
    }
    sendMessageToUser(user.get(), message, attachment);
  }

  public void sendMessageToUser(SlackUser user, String message, SlackAttachment attachment) {
    try {
      RETRYER.call(() -> sendMessage(user, message, attachment));
      metricRegistry.counter("successful-slack-dm-sends").inc();
      // Here we swallow exceptions that might be thrown by our retryer or #sendMessage().
      // We don't catch and retry any exceptions because the slack session doesn't throw us any, (it swallows them and returns null)
    } catch (Exception e) {
      LOG.error("Could not send slack message {}", attachment, e);
      metricRegistry.counter("failed-slack-dm-sends").inc();
    }
  }

  private boolean sendMessage(SlackUser user, String message, SlackAttachment attachment) {
    Optional<SlackMessageHandle<SlackMessageReply>> result = Optional.fromNullable(session.sendMessageToUser(user, message, attachment));
    if (!result.isPresent()) {
      LOG.warn("Failed to send slack message to user: {} message: {} slack result was null", user.getRealName(), attachment.toString());
      return false;
    }

    ParsedSlackReply reply = result.get().getReply();
    if (!reply.isOk()) {
      LOG.warn("Failed to send slack message to user: {} message: {} error: {}", user.getRealName(), attachment.toString(), reply.getErrorMessage());
      return false;
    }
    return true;
  }

  private boolean sendMessage(SlackChannel channel, String message, SlackAttachment attachment) {
    if (!ensureConnected()) {
      LOG.error("Could not connect to slack -- not sending message {} ({}) to {}", message, attachment, channel);
      return false;
    }

    Optional<SlackMessageHandle<SlackMessageReply>> result = Optional.fromNullable(session.sendMessage(channel, message, attachment));
    // This slack library does not throw us back any exceptions it just calls `e.printStackTrace()` and returns null
    if (!result.isPresent()) {
      LOG.warn("Failed to send slack message to channel: {} message: {} slack result was null", channel.getName(), attachment.toString());
      return false;
    }

    ParsedSlackReply reply = result.get().getReply();
    if (!reply.isOk()) {
      if (reply.getErrorMessage().equals("not_in_channel")) {
        // There is nothing the bot can do about this. It must be invited returning `true` here prevents sentries etc.
        return true;
      } else if (reply.getErrorMessage().equals("is_archived")) {
        // ignore errors sending to archived channels
        return true;
      }

      LOG.warn("Failed to send slack message to channel: {} message: {} error: {}", channel.getName(), attachment.toString(), reply.getErrorMessage());
      return false;
    }
    return true;
  }

  private boolean ensureConnected() {
    if (!session.isConnected()) {
      try {
        session.connect();
        return true;
      } catch (IOException e) {
        LOG.error("Could not connect to slack: {}", e);
        return false;
      }
    }
    return true;
  }
}

