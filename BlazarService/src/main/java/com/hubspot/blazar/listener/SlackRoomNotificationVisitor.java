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
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.BlazarUrlHelper;
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
  private final SlackSession slackSession;
  private final SlackUtils slackUtils;
  private final RepositoryBuildService repositoryBuildService;

  @Inject
  public SlackRoomNotificationVisitor(InstantMessageConfigurationService instantMessageConfigurationService,
                                      BranchService branchService,
                                      ModuleBuildService moduleBuildService,
                                      SlackSession slackSession,
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
      if (shouldSend(instantMessageConfiguration, build.getState(), previous)) {
        sendSlackMessage(instantMessageConfiguration, build);
      }
    }
  }

  private boolean shouldSend(InstantMessageConfiguration instantMessageConfiguration, RepositoryBuild.State state, Optional<RepositoryBuild> previous) {
    // OnChange
    boolean changedState = previous.isPresent() && previous.get().getState() != state;
    if (instantMessageConfiguration.getOnChange() && changedState) {
      return true;
    }
    // OnFinish
    if (instantMessageConfiguration.getOnFinish()) {
      return true;
    }
    // OnRecovery
    boolean previousFailed = previous.isPresent() && previous.get().getState() != RepositoryBuild.State.SUCCEEDED;
    if (instantMessageConfiguration.getOnRecover() && previousFailed && state == RepositoryBuild.State.SUCCEEDED) {
      return true;
    }
    // OnFailure
    if (instantMessageConfiguration.getOnFail() && FAILED_REPO_STATES.contains(state)) {
      return true;
    }
    return false;
  }

  private void sendSlackMessage(InstantMessageConfiguration instantMessageConfiguration, RepositoryBuild build) {
    SlackChannel slackChannel = slackSession.findChannelByName(instantMessageConfiguration.getChannelName());
    slackSession.sendMessage(slackChannel, "", slackUtils.buildSlackAttachment(build));
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
    SlackChannel slackChannel = slackSession.findChannelByName(instantMessageConfiguration.getChannelName());
    slackSession.sendMessage(slackChannel, "", slackUtils.buildSlackAttachment(build));
  }

  private boolean shouldSend(InstantMessageConfiguration instantMessageConfiguration, ModuleBuild.State state, Optional<ModuleBuild> previous, ModuleBuild thisBuild) {
    final String logBase = String.format("Not sending Slack notification: ModuleBuild %s,", String.valueOf(thisBuild.getId()));
    // OnChange
    boolean changedState = previous.isPresent() && previous.get().getState() != state;
    if (instantMessageConfiguration.getOnChange() && changedState) {
      return true;
    }
    LOG.info("{} OnChange {}, changedState {}", logBase, instantMessageConfiguration.getOnChange(), changedState);
    // OnFinish
    if (instantMessageConfiguration.getOnFinish()) {
      return true;
    }
    LOG.info("{} OnFinish {}", logBase, instantMessageConfiguration.getOnFinish());
    // OnRecovery
    boolean previousFailed = previous.isPresent() && previous.get().getState() != ModuleBuild.State.SUCCEEDED;
    if (instantMessageConfiguration.getOnRecover() && previousFailed && state == ModuleBuild.State.SUCCEEDED) {
      return true;
    }
    LOG.info("{} OnRecover {}, previousFailed {}, thisFailed {}", logBase, instantMessageConfiguration.getOnRecover(), previousFailed, state == ModuleBuild.State.SUCCEEDED);
    // OnFailure
    if (instantMessageConfiguration.getOnFail() && FAILED_MODULE_STATES.contains(state)) {
      return true;
    }
    LOG.info("{} onFail {}, thisFailed {}", logBase, instantMessageConfiguration.getOnFail(), FAILED_MODULE_STATES.contains(state));

    return false;
  }
}
