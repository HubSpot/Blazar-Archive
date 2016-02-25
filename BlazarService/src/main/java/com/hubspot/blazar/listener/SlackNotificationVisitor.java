package com.hubspot.blazar.listener;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.externalservice.slack.SlackAttachment;
import com.hubspot.blazar.base.externalservice.slack.SlackAttachmentField;
import com.hubspot.blazar.base.externalservice.slack.SlackMessage;
import com.hubspot.blazar.base.slack.SlackConfiguration;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.service.SlackConfigurationService;
import com.hubspot.blazar.integration.slack.SlackClient;
import com.hubspot.blazar.util.BlazarUrlHelper;

@Singleton
public class SlackNotificationVisitor implements RepositoryBuildVisitor, ModuleBuildVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationVisitor.class);
  private static final Set<RepositoryBuild.State> FAILED_REPO_STATES = ImmutableSet.of(RepositoryBuild.State.CANCELLED, RepositoryBuild.State.FAILED, RepositoryBuild.State.UNSTABLE);
  private static final Set<ModuleBuild.State> FAILED_MODULE_STATES = ImmutableSet.of(ModuleBuild.State.CANCELLED, ModuleBuild.State.FAILED);
  private static final Optional<String> ABSENT_STRING = Optional.absent();

  private SlackConfigurationService slackConfigurationService;
  private final BranchService branchService;
  private ModuleService moduleService;
  private final ModuleBuildService moduleBuildService;
  private final BlazarUrlHelper blazarUrlHelper;
  private final SlackClient slackClient;
  private final RepositoryBuildService repositoryBuildService;

  @Inject
  public SlackNotificationVisitor(SlackConfigurationService slackConfigurationService,
                                  BranchService branchService,
                                  ModuleService moduleService,
                                  ModuleBuildService moduleBuildService,
                                  BlazarUrlHelper blazarUrlHelper,
                                  SlackClient slackClient,
                                  RepositoryBuildService repositoryBuildService) {
    this.slackConfigurationService = slackConfigurationService;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleBuildService = moduleBuildService;
    this.blazarUrlHelper = blazarUrlHelper;
    this.slackClient = slackClient;
    this.repositoryBuildService = repositoryBuildService;
  }


  @Override
  public void visit(RepositoryBuild build) throws Exception {
    Set<SlackConfiguration> configurationSet = slackConfigurationService.getAllWithBranchId(build.getBranchId());
    Optional<RepositoryBuild> previous = repositoryBuildService.getPreviousBuild(build);
    for (SlackConfiguration slackConfiguration : configurationSet) {
      if (shouldSend(slackConfiguration, build.getState(), previous)) {
        GitInfo gitInfo = branchService.get(build.getBranchId()).get();
        sendSlackMessage(slackConfiguration, build, gitInfo);
      }
    }
  }

  private boolean shouldSend(SlackConfiguration slackConfiguration, RepositoryBuild.State state, Optional<RepositoryBuild> previous) {
    // OnChange
    boolean changedState = previous.isPresent() && previous.get().getState() == state;
    if (slackConfiguration.getOnChange() && changedState) {
      return true;
    }
    // OnFinish
    if (slackConfiguration.getOnFinish() && state.isComplete()) {
      return true;
    }
    // OnRecovery
    boolean previousFailed = previous.isPresent() && previous.get().getState().isComplete() && previous.get().getState() == RepositoryBuild.State.SUCCEEDED;
    if (slackConfiguration.getOnRecover() && previousFailed && state == RepositoryBuild.State.SUCCEEDED) {
      return true;
    }
    // OnFailure
    if (slackConfiguration.getOnFail() && FAILED_REPO_STATES.contains(state)) {
      return true;
    }
    return false;
  }

  private void sendSlackMessage(SlackConfiguration slackConfiguration, RepositoryBuild build, GitInfo gitInfo) {
    String fallback = String.format("Module Build %s-%s finished with state %s", gitInfo.getRepository(), gitInfo.getBranch(), build.getState().toString().toLowerCase());
    Optional<String> color = ABSENT_STRING;
    switch (build.getState()) {
      case SUCCEEDED:
        color = Optional.of("good");
        break;
      case CANCELLED:
        color = Optional.of("warning");
        break;
      case UNSTABLE:
        color = Optional.of("danger");
        break;
      case FAILED:
        color = Optional.of("danger");
        break;
    }

    Optional<String> title = Optional.of(fallback);
    Optional<String> link = Optional.of(blazarUrlHelper.getBlazarUiLink(build));

    SlackAttachment attachment = new SlackAttachment(fallback, color, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, title, link, ABSENT_STRING, Collections.<SlackAttachmentField>emptyList(), ABSENT_STRING);
    SlackMessage message = new SlackMessage(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, slackConfiguration.getChannelName(), Lists.newArrayList(attachment));
    slackClient.sendMessage(message);
  }


  @Override
  public void visit(ModuleBuild build) throws Exception {
    Set<SlackConfiguration> configurationSet = slackConfigurationService.getAllWithModuleId(build.getModuleId());
    Optional<ModuleBuild> previous = moduleBuildService.getPreviousBuild(build);
    for (SlackConfiguration slackConfiguration : configurationSet) {
      if (shouldSend(slackConfiguration, build.getState(), previous, build)) {
        Optional<GitInfo> gitInfo = branchService.get(repositoryBuildService.get(build.getRepoBuildId()).get().getBranchId());
        if (!gitInfo.isPresent()) {
          throw new IllegalArgumentException(String.format("Tried to send slack message for moduleBuild %s with no associated branch", build.getId().get()));
        }
        sendSlackMessage(slackConfiguration, build, gitInfo.get());
      }
    }
  }

  private void sendSlackMessage(SlackConfiguration slackConfiguration, ModuleBuild build, GitInfo gitInfo) {
    Module module = moduleService.get(build.getModuleId()).get();
    String fallback = String.format("Module Build %s-%s-%s finished with state %s", gitInfo.getRepository(), gitInfo.getBranch(), module.getName(), build.getState().toString().toLowerCase());
    Optional<String> color = ABSENT_STRING;
    switch (build.getState()) {
      case SUCCEEDED:
        color = Optional.of("good");
        break;
      case SKIPPED:
        color = Optional.of("warning");
        break;
      case CANCELLED:
        color = Optional.of("warning");
        break;
      case FAILED:
        color = Optional.of("danger");
        break;
    }

    Optional<String> title = Optional.of(fallback);
    Optional<String> link = Optional.of(blazarUrlHelper.getBlazarUiLink(build));

    SlackAttachment attachment = new SlackAttachment(fallback, color, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, title, link, ABSENT_STRING, Collections.<SlackAttachmentField>emptyList(), ABSENT_STRING);
    SlackMessage message = new SlackMessage(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, slackConfiguration.getChannelName(), Lists.newArrayList(attachment));
    slackClient.sendMessage(message);
  }

  private boolean shouldSend(SlackConfiguration slackConfiguration, ModuleBuild.State state, Optional<ModuleBuild> previous, ModuleBuild thisBuild) {
    final String logBase = String.format("Not sending Slack notification: ModuleBuild %s,", String.valueOf(thisBuild.getId()));
    // OnChange
    boolean changedState = previous.isPresent() && previous.get().getState() == state;
    if (slackConfiguration.getOnChange() && changedState) {
      return true;
    }
    LOG.info("{} OnChange {}, changedState {}", logBase, slackConfiguration.getOnChange(), changedState);
    // OnFinish
    if (slackConfiguration.getOnFinish() && state.isComplete()) {
      return true;
    }
    LOG.info("{} OnFinish {}, isComplete {}", logBase, slackConfiguration.getOnFinish(), state.isComplete());
    // OnRecovery
    boolean previousFailed = previous.isPresent() && previous.get().getState().isComplete() && previous.get().getState() == ModuleBuild.State.SUCCEEDED;
    if (slackConfiguration.getOnRecover() && previousFailed && state == ModuleBuild.State.SUCCEEDED) {
      return true;
    }
    LOG.info("{} OnRecover {}, previousFailed {}, thisFailed {}", logBase, slackConfiguration.getOnRecover(), previousFailed, state == ModuleBuild.State.SUCCEEDED);
    // OnFailure
    if (slackConfiguration.getOnFail() && FAILED_MODULE_STATES.contains(state)) {
      return true;
    }
    LOG.info("{} onFail {}, thisFailed {}", logBase, slackConfiguration.getOnFail(), FAILED_MODULE_STATES.contains(state));

    return false;
  }
}
