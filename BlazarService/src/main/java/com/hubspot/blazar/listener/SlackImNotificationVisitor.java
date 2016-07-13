package com.hubspot.blazar.listener;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
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

  private Optional<SlackSession> slackSession;
  private SlackUtils slackUtils;

  @Inject
  public SlackImNotificationVisitor (Optional<SlackSession> slackSession,
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
    if (slackSession.isPresent()) {
      LOG.info("Sending slack notification for repo build {}", build.getId().get());
      CommitInfo commitInfo = build.getCommitInfo().get();
      Optional<SlackUser> user = Optional.fromNullable(slackSession.get().findUserByEmail(commitInfo.getCurrent().getAuthor().getEmail()));
      if (user.isPresent()) {
        slackSession.get().sendMessageToUser(user.get(), "", slackUtils.buildSlackAttachment(build));
      } else {
        LOG.error("Could not find user {} for message about repo build {}", commitInfo.getCurrent().getAuthor().getEmail(), build.getId().get());
      }
    }
  }
}
