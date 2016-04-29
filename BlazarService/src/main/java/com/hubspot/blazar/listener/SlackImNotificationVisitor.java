package com.hubspot.blazar.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.util.SlackUtils;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;

public class SlackImNotificationVisitor implements RepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(SlackImNotificationVisitor.class);
  private final BlazarSlackConfiguration blazarSlackConfig;

  private SlackSession slackSession;
  private SlackUtils slackUtils;

  @Inject
  public SlackImNotificationVisitor (SlackSession slackSession,
                                     BlazarConfiguration blazarConfiguration,
                                     SlackUtils slackUtils) {
    this.slackSession = slackSession;
    this.slackUtils = slackUtils;
    this.blazarSlackConfig = blazarConfiguration.getSlackConfiguration().get();
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (!build.getCommitInfo().isPresent() || !blazarSlackConfig.getImWhitelist().contains(build.getCommitInfo().get().getCurrent().getAuthor().getEmail())) {
      LOG.info("No commitinfo present, or user is not in whitelist");
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
    LOG.info("Sending slack notification for repo build {}", build.getId().get());
    CommitInfo commitInfo = build.getCommitInfo().get();
    SlackUser user = slackSession.findUserByEmail(commitInfo.getCurrent().getAuthor().getEmail());
    slackSession.sendMessageToUser(user, "", slackUtils.buildSlackAttachment(build));
  }
}
