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
  private final ModuleBuildService moduleBuildService;
  private final BlazarUrlHelper blazarUrlHelper;
  private final SlackClient slackClient;
  private final RepositoryBuildService repositoryBuildService;

  @Inject
  public SlackNotificationVisitor(SlackConfigurationService slackConfigurationService,
                                  BranchService branchService,
                                  ModuleBuildService moduleBuildService,
                                  BlazarUrlHelper blazarUrlHelper,
                                  SlackClient slackClient,
                                  RepositoryBuildService repositoryBuildService) {
    this.slackConfigurationService = slackConfigurationService;
    this.branchService = branchService;
    this.moduleBuildService = moduleBuildService;
    this.blazarUrlHelper = blazarUrlHelper;
    this.slackClient = slackClient;
    this.repositoryBuildService = repositoryBuildService;
  }


  @Override
  public void visit(RepositoryBuild build) throws Exception {
    Set<SlackConfiguration> configurationSet = slackConfigurationService.getAllWithBranchId(build.getBranchId());
    for (SlackConfiguration slackConfiguration : configurationSet) {
      Optional<RepositoryBuild> previous = repositoryBuildService.getPreviousBuild(build);
      if (previous.isPresent() && !shouldSend(slackConfiguration, build.getState(), previous.get())) {
        continue;
      }
      GitInfo gitInfo = branchService.get(build.getBranchId()).get();
      sendSlackMessage(slackConfiguration, build, gitInfo);
    }
  }

  private boolean shouldSend(SlackConfiguration slackConfiguration, RepositoryBuild.State state, RepositoryBuild previous) {
    // OnChange
    boolean changedState = previous.getState() == state;
    if (slackConfiguration.getOnChange() && changedState) {
      return true;
    }
    // OnFinish
    if (slackConfiguration.getOnFinish() && state.isComplete()) {
      return true;
    }
    // OnRecovery
    boolean previousFailed = previous.getState().isComplete() && previous.getState() == RepositoryBuild.State.SUCCEEDED;
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
    String fallback = String.format("Module Build %s-%s-%s finished with state %s", gitInfo.getRepository(), gitInfo.getRepository(), gitInfo.getBranch(), build.getState().toString());
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
    Set<SlackConfiguration> configurationSet = slackConfigurationService.getAllWithBranchId(build.getRepoBuildId());
    for (SlackConfiguration slackConfiguration : configurationSet) {
      Optional<ModuleBuild> previous = moduleBuildService.getPreviousBuild(build);
      if (previous.isPresent() && !shouldSend(slackConfiguration, build.getState(), previous.get())) {
        continue;
      }
      Optional<GitInfo> gitInfo = branchService.get(repositoryBuildService.get(build.getRepoBuildId()).get().getBranchId());
      if (!gitInfo.isPresent()) {
        throw new IllegalArgumentException(String.format("Tried to send slack message for moduleBuild %s with no associated branch", build.getId().get()));
      }
      sendSlackMessage(slackConfiguration, build, gitInfo.get());
    }

  }

  private void sendSlackMessage(SlackConfiguration slackConfiguration, ModuleBuild build, GitInfo gitInfo) {
    String fallback = String.format("Module Build %s-%s-%s finished with state %s", gitInfo.getRepository(), gitInfo.getRepository(), gitInfo.getBranch(), build.getState().toString());
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

  private boolean shouldSend(SlackConfiguration slackConfiguration, ModuleBuild.State state, ModuleBuild previous) {
    // OnChange
    boolean changedState = previous.getState() == state;
    if (slackConfiguration.getOnChange() && changedState) {
      return true;
    }
    // OnFinish
    if (slackConfiguration.getOnFinish() && state.isComplete()) {
      return true;
    }
    // OnRecovery
    boolean previousFailed = previous.getState().isComplete() && previous.getState() == ModuleBuild.State.SUCCEEDED;
    if (slackConfiguration.getOnRecover() && previousFailed && state == ModuleBuild.State.SUCCEEDED) {
      return true;
    }
    // OnFailure
    if (slackConfiguration.getOnFail() && FAILED_MODULE_STATES.contains(state)) {
      return true;
    }

    return false;
  }
}
