package com.hubspot.blazar.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.notifications.InstantMessageConfiguration;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InstantMessageConfigurationService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.BlazarSlackClient;
import com.hubspot.blazar.util.SlackMessageBuildingUtils;

@Singleton
public class SlackRoomNotificationVisitor implements RepositoryBuildVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(SlackRoomNotificationVisitor.class);

  private InstantMessageConfigurationService instantMessageConfigurationService;
  private BlazarSlackClient blazarSlackClient;
  private final SlackMessageBuildingUtils slackMessageBuildingUtils;
  private final RepositoryBuildService repositoryBuildService;

  @Inject
  public SlackRoomNotificationVisitor(
      InstantMessageConfigurationService instantMessageConfigurationService,
      BlazarSlackClient blazarSlackClient,
      SlackMessageBuildingUtils slackMessageBuildingUtils,
      RepositoryBuildService repositoryBuildService) {

    this.instantMessageConfigurationService = instantMessageConfigurationService;
    this.blazarSlackClient = blazarSlackClient;
    this.slackMessageBuildingUtils = slackMessageBuildingUtils;
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
      if (shouldSendMessage(instantMessageConfiguration, build.getState(), previous, build)) {
        blazarSlackClient.sendMessageToChannel(instantMessageConfiguration.getChannelName(), "", slackMessageBuildingUtils.buildSlackAttachment(build));
      }
    }
  }

  private boolean shouldSendMessage(
      InstantMessageConfiguration instantMessageConfiguration,
      RepositoryBuild.State state,
      Optional<RepositoryBuild> previous,
      RepositoryBuild build) {

    boolean shouldSend = false;
    final String logBase = String.format("Will send slack notification: RepoBuild %s,", String.valueOf(build.getId()));
    // OnChange
    boolean isChanged = previous.isPresent() && previous.get().getState() != state;
    if (instantMessageConfiguration.getOnChange() && isChanged) {
      LOG.info("{} OnChange {}, State has changed from {} to {}", logBase, instantMessageConfiguration.getOnChange(), previous.get().getState(), state);
      shouldSend = true;
    }
    // OnSuccess
    if (instantMessageConfiguration.getOnFinish() && state.equals(RepositoryBuild.State.SUCCEEDED)) {
      LOG.info("{} OnSuccess {}", logBase, instantMessageConfiguration.getOnFinish());
      shouldSend = true;
    }
    // OnFailure
    if (instantMessageConfiguration.getOnFail() && state.isFailed()) {
      LOG.info("{} OnFail {}, thisFailed {}", logBase, instantMessageConfiguration.getOnFail(), state.isFailed());
      shouldSend = true;
    }
    if (!shouldSend) {
      LOG.debug("Not sending slack message for RepoBuild {}", build.getId());
    }
    return shouldSend;
  }
}
