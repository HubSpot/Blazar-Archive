package com.hubspot.blazar.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.externalservice.slack.SlackMessage;
import com.hubspot.blazar.externalservice.slack.SlackUser;
import com.hubspot.blazar.integration.slack.SlackClient;

public class SlackImNotificationVisitor implements RepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(SlackImNotificationVisitor.class);

  private final SlackClient slackClient;
  private BranchService branchService;

  @Inject
  public SlackImNotificationVisitor (SlackClient slackClient,
                                     BranchService branchService) {
    this.slackClient = slackClient;
    this.branchService = branchService;
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (!build.getCommitInfo().isPresent()) {
      LOG.debug("No commitinfo present, cant send a message for this build");
      return;
    }
    boolean wasPush = build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH ;
    boolean wasBranchCreation = build.getBuildTrigger().getType() == BuildTrigger.Type.BRANCH_CREATION;
    if (!(wasPush || wasBranchCreation)) {
      LOG.debug("Not sending messages for triggers other than push or branch creation at this time.");
      return;
    }

    if (build.getState() == RepositoryBuild.State.IN_PROGRESS) {
      handleInProgress(build);
    }

    if (build.getState().isComplete()) {
      if (build.getState() == RepositoryBuild.State.SUCCEEDED) {
        handleSuccess(build);
      } else {
        handleFailure(build);
      }
    }
  }

  private void handleInProgress(RepositoryBuild build) throws IOException {
    CommitInfo commitInfo = build.getCommitInfo().get();
    Optional<SlackUser> user = slackClient.getUserByEmail(commitInfo.getCurrent().getAuthor().getEmail());
    if (!user.isPresent()) {
      LOG.info("No slack user found for commit %s not sending a message", commitInfo.getCurrent().getId());
      return;
    }
    String message = String.format("A build of %s you triggered is now in progress", getBuildString(build));
    slackClient.sendMessage(buildForIm(message, user.get()));
  }

  private void handleSuccess(RepositoryBuild build) throws IOException {
    CommitInfo commitInfo = build.getCommitInfo().get();
    Optional<SlackUser> user = slackClient.getUserByEmail(commitInfo.getCurrent().getAuthor().getEmail());
    if (!user.isPresent()) {
      LOG.info("No slack user found for commit %s not sending a message", commitInfo.getCurrent().getId());
      return;
    }
    String message = String.format("A build of %s you triggered successfully completed", getBuildString(build));
    slackClient.sendMessage(buildForIm(message, user.get()));
  }

  private void handleFailure(RepositoryBuild build) throws IOException {
    CommitInfo commitInfo = build.getCommitInfo().get();
    Optional<SlackUser> user = slackClient.getUserByEmail(commitInfo.getCurrent().getAuthor().getEmail());
    if (!user.isPresent()) {
      LOG.info("No slack user found for commit %s not sending a message", commitInfo.getCurrent().getId());
      return;
    }
    String message = String.format("A build of %s you triggered failed with state: %s", getBuildString(build), build.getState().toString().toLowerCase());
    slackClient.sendMessage(buildForIm(message, user.get()));
  }

  private String getBuildString(RepositoryBuild build) {
    GitInfo gitInfo = branchService.get(build.getBranchId()).get();
    return String.format("%s#%s", gitInfo.getRepository(), gitInfo.getBranch());
  }

  private SlackMessage buildForIm(String message, SlackUser user) {
    SlackMessage.Builder builder = SlackMessage.newBuilder();
    builder.setAs_user(true);
    builder.setText(message);
    builder.setChannel(user.getId());
    return builder.build();
  }
}
