package com.hubspot.blazar.listener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.util.SlackUtils;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

public class SlackImNotificationVisitor implements RepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(SlackImNotificationVisitor.class);
  private final BlazarSlackConfiguration blazarSlackConfig;

  private final Optional<SlackSession> slackSession;
  private final MetricRegistry metricRegistry;
  private final SlackUtils slackUtils;

  @Inject
  public SlackImNotificationVisitor (Optional<SlackSession> slackSession,
                                     MetricRegistry metricRegistry,
                                     BlazarConfiguration blazarConfiguration,
                                     SlackUtils slackUtils) {
    this.slackSession = slackSession;
    this.metricRegistry = metricRegistry;
    this.slackUtils = slackUtils;
    this.blazarSlackConfig = blazarConfiguration.getSlackConfiguration().get();
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (!build.getCommitInfo().isPresent() || !blazarSlackConfig.getImWhitelist().contains(build.getCommitInfo().get().getCurrent().getAuthor().getEmail())) {
      LOG.info("No commitInfo present, or user is not in whiteList");
      return;
    }
    boolean wasPush = build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH;
    boolean wasBranchCreation = build.getBuildTrigger().getType() == BuildTrigger.Type.BRANCH_CREATION;
    if (!(wasPush || wasBranchCreation)) {
      LOG.info("Not sending messages for triggers other than push or branch creation at this time.");
      return;
    }

    if (!build.getState().isComplete() || build.getState().equals(RepositoryBuild.State.SUCCEEDED) || build.getState().equals(RepositoryBuild.State.CANCELLED) ) {
      LOG.info("Not sending notifications for builds in non-terminal states or on success / cancellation");
      return;
    }
    sendSlackMessageWithRetries(build, slackUtils.buildSlackAttachment(build));
  }

  private void sendSlackMessageWithRetries(RepositoryBuild build, SlackAttachment attachment) {
    try {
      SlackUtils.makeSlackMessageSendingRetryer().call(() -> sendSlackMessage(build, attachment));
      metricRegistry.meter("successful-slack-dm-sends").mark();
    } catch (Exception e){
      LOG.error("Could not send slack message {}", attachment, e);
      metricRegistry.meter("failed-slack-dm-sends").mark();
    }
  }

  private boolean sendSlackMessage(RepositoryBuild build, SlackAttachment attachment) {
    String email = build.getCommitInfo().get().getCurrent().getAuthor().getEmail();
    if (!slackSession.isPresent()) {
      LOG.debug("No slack session present, not sending message {} to user {}", attachment.toString(), email);
      return true;
    }

    Optional<SlackUser> user = Optional.fromNullable(slackSession.get().findUserByEmail(email));
    if (!user.isPresent()) {
      LOG.error("Could not find user {} for message about repo build {}", email, build.getId().get());
      return false;
    }

    SlackMessageHandle<SlackMessageReply> result = slackSession.get().sendMessageToUser(user.get(), "", attachment);
    if (result == null) {
      LOG.warn("Failed to send slack message to user: {} message: {} slack response was null", email, attachment.toString());
      return false;
    } else if (!result.getReply().isOk()) {
      LOG.warn("Failed to send slack message to user: {} message: {} error: {}", email, attachment.toString(), result.getReply().getErrorMessage());
      return false;
    }
    return true;
  }
}
