package com.hubspot.blazar.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.notifications.InstantMessageConfiguration;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InstantMessageConfigurationService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.SlackUtils;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

@Singleton
public class SlackRoomNotificationVisitor implements RepositoryBuildVisitor, ModuleBuildVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(SlackRoomNotificationVisitor.class);
  private static final Set<RepositoryBuild.State> FAILED_REPO_STATES = ImmutableSet.of(RepositoryBuild.State.CANCELLED, RepositoryBuild.State.FAILED, RepositoryBuild.State.UNSTABLE);
  private static final Set<ModuleBuild.State> FAILED_MODULE_STATES = ImmutableSet.of(ModuleBuild.State.CANCELLED, ModuleBuild.State.FAILED);

  private InstantMessageConfigurationService instantMessageConfigurationService;
  private final MetricRegistry metricRegistry;
  private final ModuleBuildService moduleBuildService;
  private final Optional<SlackSession> slackSession;
  private final SlackUtils slackUtils;
  private final RepositoryBuildService repositoryBuildService;

  @Inject
  public SlackRoomNotificationVisitor(InstantMessageConfigurationService instantMessageConfigurationService,
                                      MetricRegistry metricRegistry,
                                      ModuleBuildService moduleBuildService,
                                      Optional<SlackSession> slackSession,
                                      SlackUtils slackUtils,
                                      RepositoryBuildService repositoryBuildService) {
    this.instantMessageConfigurationService = instantMessageConfigurationService;
    this.metricRegistry = metricRegistry;
    this.moduleBuildService = moduleBuildService;
    this.slackSession = slackSession;
    this.slackUtils = slackUtils;
    this.repositoryBuildService = repositoryBuildService;
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (!(build.getState().isComplete())) {
      return;
    }
    Set<InstantMessageConfiguration> configurationSet = instantMessageConfigurationService.getAllWithBranchId(build.getBranchId());
    Optional<RepositoryBuild> previous = repositoryBuildService.getPreviousBuild(build);
    for (InstantMessageConfiguration instantMessageConfiguration : configurationSet) {
      if (shouldSendRepositoryBuild(instantMessageConfiguration, build.getState(), previous, build)) {
        sendSlackMessageWithRetries(instantMessageConfiguration.getChannelName(), slackUtils.buildSlackAttachment(build));
      }
    }
  }

  @Override
  public void visit(ModuleBuild build) throws Exception {
    if (!(build.getState().isComplete())) {
      return;
    }
    Set<InstantMessageConfiguration> configurationSet = instantMessageConfigurationService.getAllWithModuleId(build.getModuleId());
    Optional<ModuleBuild> previous = moduleBuildService.getPreviousBuild(build);
    for (InstantMessageConfiguration instantMessageConfiguration : configurationSet) {
      if (shouldSendModuleBuild(instantMessageConfiguration, build.getState(), previous, build)) {
        sendSlackMessageWithRetries(instantMessageConfiguration.getChannelName(), slackUtils.buildSlackAttachment(build));
      }
    }
  }

  private void sendSlackMessageWithRetries(String channelName, SlackAttachment attachment) {
    try {
      SlackUtils.makeSlackMessageSendingRetryer().call(() -> sendSlackMessage(channelName, attachment));
      metricRegistry.meter("successful-slack-channel-sends").mark();
    } catch (Exception e) {
      metricRegistry.meter("failed-slack-channel-sends").mark();
      LOG.error("Could not send slack message {}", attachment, e);
    }
  }

  private boolean sendSlackMessage(String channelName, SlackAttachment attachment) {
    if (!slackSession.isPresent()) {
      LOG.debug("No slack session present, not sending message {} to channel {}", attachment.toString(), channelName);
      return true;
    }

    Optional<SlackChannel> slackChannel = Optional.fromNullable(slackSession.get().findChannelByName(channelName));
    if (!slackChannel.isPresent()) {
      LOG.warn("No slack channel found for name {}", channelName);
      return false;
    }

    SlackMessageHandle<SlackMessageReply> result = slackSession.get().sendMessage(slackChannel.get(), "", attachment);
    if (result == null)  {
      LOG.warn("Failed to send slack message to channel: {} message: {} slack result was null", channelName, attachment.toString());
      return false;
    } else if (!result.getReply().isOk()) {
      LOG.warn("Failed to send slack message to channel: {} message: {} error: {}", channelName, attachment.toString(), result.getReply().getErrorMessage());
      return false;
    }
    return true;
  }

  private static boolean shouldSendRepositoryBuild(InstantMessageConfiguration instantMessageConfiguration, RepositoryBuild.State state, Optional<RepositoryBuild> previous, RepositoryBuild build) {
    boolean shouldSend = false;
    final String logBase = String.format("Will send slack notification: RepoBuild %s,", String.valueOf(build.getId()));
    // OnChange
    if (instantMessageConfiguration.getOnChange() && previous.isPresent() && previous.get().getState() != state) {
      LOG.info("{} OnChange {}, changedState {}", logBase, instantMessageConfiguration.getOnChange(), previous.get().getState() == state);
      shouldSend = true;
    }
    // OnSuccess
    if (instantMessageConfiguration.getOnFinish() && state.equals(RepositoryBuild.State.SUCCEEDED)) {
      LOG.info("{} OnSuccess {}", logBase, instantMessageConfiguration.getOnFinish());
      shouldSend = true;
    }
    // OnFailure
    if (instantMessageConfiguration.getOnFail() && FAILED_REPO_STATES.contains(state)) {
      LOG.info("{} OnFail {}, thisFailed {}", logBase, instantMessageConfiguration.getOnFail(), FAILED_REPO_STATES.contains(state));
      shouldSend = true;
    }
    if (!shouldSend) {
      LOG.debug("Not sending slack message for RepoBuild {}", build.getId());
    }
    return shouldSend;
  }

  private static boolean shouldSendModuleBuild(InstantMessageConfiguration instantMessageConfiguration, ModuleBuild.State state, Optional<ModuleBuild> previous, ModuleBuild thisBuild) {
    final String logBase = String.format("Will send slack notification: ModuleBuild %s,", String.valueOf(thisBuild.getId()));
    boolean shouldSend = false;
    // OnChange
    if (instantMessageConfiguration.getOnChange() && previous.isPresent() && previous.get().getState() != state) {
      LOG.info("{} OnChange {}, changedState {}", logBase, instantMessageConfiguration.getOnChange(), previous.get().getState() != state);
      shouldSend = true;
    }
    // OnFinish
    if (instantMessageConfiguration.getOnFinish()) {
      LOG.info("{} OnFinish {}", logBase, instantMessageConfiguration.getOnFinish());
      shouldSend = true;
    }
    // OnFailure
    if (instantMessageConfiguration.getOnFail() && FAILED_MODULE_STATES.contains(state)) {
      LOG.info("{} onFail {}, thisFailed {}", logBase, instantMessageConfiguration.getOnFail(), FAILED_MODULE_STATES.contains(state));
      shouldSend = true;
    }
    if (!shouldSend) {
      LOG.debug("Not sending slack message for ModuleBuild {}", thisBuild.getId());
    }
    return shouldSend;
  }
}
