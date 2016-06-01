package com.hubspot.blazar.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.notifications.InstantMessageConfiguration;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.InstantMessageConfigurationService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.SlackUtils;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;

@Singleton
public class SlackRoomNotificationVisitor implements RepositoryBuildVisitor, ModuleBuildVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(SlackRoomNotificationVisitor.class);
  private static final Set<RepositoryBuild.State> FAILED_REPO_STATES = ImmutableSet.of(RepositoryBuild.State.CANCELLED, RepositoryBuild.State.FAILED, RepositoryBuild.State.UNSTABLE);
  private static final Set<ModuleBuild.State> FAILED_MODULE_STATES = ImmutableSet.of(ModuleBuild.State.CANCELLED, ModuleBuild.State.FAILED);

  private InstantMessageConfigurationService instantMessageConfigurationService;
  private final BranchService branchService;
  private final ModuleBuildService moduleBuildService;
  private final Optional<SlackSession> slackSession;
  private final SlackUtils slackUtils;
  private final RepositoryBuildService repositoryBuildService;

  @Inject
  public SlackRoomNotificationVisitor(InstantMessageConfigurationService instantMessageConfigurationService,
                                      BranchService branchService,
                                      ModuleBuildService moduleBuildService,
                                      Optional<SlackSession> slackSession,
                                      SlackUtils slackUtils,
                                      RepositoryBuildService repositoryBuildService) {
    this.instantMessageConfigurationService = instantMessageConfigurationService;
    this.branchService = branchService;
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
      if (shouldSend(instantMessageConfiguration, build.getState(), previous, build)) {
        sendSlackMessage(instantMessageConfiguration, build);
      }
    }
  }

  private boolean shouldSend(InstantMessageConfiguration instantMessageConfiguration, RepositoryBuild.State state, Optional<RepositoryBuild> previous, RepositoryBuild build) {
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
      LOG.debug("Not sending slack message for RepoBuild {} should send: {}", build.getId(), shouldSend);
    }
    return shouldSend;
  }

  private void sendSlackMessage(InstantMessageConfiguration instantMessageConfiguration, RepositoryBuild build) {
    if (slackSession.isPresent()) {
      Optional<SlackChannel> slackChannel = Optional.fromNullable(slackSession.get().findChannelByName(instantMessageConfiguration.getChannelName()));
      if (slackChannel.isPresent()) {
        slackSession.get().sendMessage(slackChannel.get(), "", slackUtils.buildSlackAttachment(build));
      } else {
        LOG.warn("No slack channel found for name {}", instantMessageConfiguration.getChannelName());
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
      if (shouldSend(instantMessageConfiguration, build.getState(), previous, build)) {
        GitInfo gitInfo = branchService.get(repositoryBuildService.get(build.getRepoBuildId()).get().getBranchId()).get();
        sendSlackMessage(instantMessageConfiguration, build);
      }
    }
  }

  private void sendSlackMessage(InstantMessageConfiguration instantMessageConfiguration, ModuleBuild build) {
    if (slackSession.isPresent()) {
      SlackChannel slackChannel = slackSession.get().findChannelByName(instantMessageConfiguration.getChannelName());
      slackSession.get().sendMessage(slackChannel, "", slackUtils.buildSlackAttachment(build));
    }
  }

  private boolean shouldSend(InstantMessageConfiguration instantMessageConfiguration, ModuleBuild.State state, Optional<ModuleBuild> previous, ModuleBuild thisBuild) {
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
      LOG.debug("Not sending slack message for ModuleBuild {} shouldSend: {}", thisBuild.getId(), shouldSend);
    }
    return shouldSend;
  }
}
