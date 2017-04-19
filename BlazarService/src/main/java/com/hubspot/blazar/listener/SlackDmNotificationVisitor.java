package com.hubspot.blazar.listener;


import static com.hubspot.blazar.base.RepositoryBuild.State.FAILED;
import static com.hubspot.blazar.base.RepositoryBuild.State.UNSTABLE;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildMetadata;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackDirectMessageConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.util.BlazarSlackClient;
import com.hubspot.blazar.util.SlackMessageBuildingUtils;

/**
 * Direct messages slack users about the state of builds caused by their pushes
 */
public class SlackDmNotificationVisitor implements RepositoryBuildVisitor {
  // Cancelled requires a user action - likely the user who would be slacked anyway so it is not worth messaging about
  private static final Set<RepositoryBuild.State> SLACK_WORTHY_FAILING_STATES = ImmutableSet.of(FAILED, UNSTABLE);
  private static final Logger LOG = LoggerFactory.getLogger(SlackDmNotificationVisitor.class);
  private final BlazarSlackDirectMessageConfiguration slackDirectMessageConfig;

  private final BranchService branchService;
  private final SlackMessageBuildingUtils slackMessageBuildingUtils;
  private final BlazarSlackClient blazarSlackClient;

  @Inject
  public SlackDmNotificationVisitor(BlazarConfiguration blazarConfiguration,
                                    BranchService branchService,
                                    SlackMessageBuildingUtils slackMessageBuildingUtils,
                                    BlazarSlackClient blazarSlackClient) {
    this.branchService = branchService;
    this.slackMessageBuildingUtils = slackMessageBuildingUtils;
    this.blazarSlackClient = blazarSlackClient;
    this.slackDirectMessageConfig = blazarConfiguration.getSlackConfiguration().get().getDirectMessageConfiguration();
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (!build.getState().isComplete()) {
      return;
    }

    Set<String> userEmailsToSendMessagesTo = getUserEmailsToDirectlyNotify(build);

    boolean shouldSendMessage = shouldSendMessage(build, userEmailsToSendMessagesTo);
    if (shouldSendMessage) {
      for (String email : userEmailsToSendMessagesTo) {
        blazarSlackClient.sendMessageToUser(email, "A build started by your code push failed", slackMessageBuildingUtils.buildSlackAttachment(build));
      }
    }
  }

  private Set<String> getUserEmailsToDirectlyNotify(RepositoryBuild build) {
    if (!build.getCommitInfo().isPresent()) {
      LOG.info("No commit info present cannot determine user to slack");
      return Collections.emptySet();
    }

    Set<String> directNotifyEmails = build.getBuildMetadata().getUser().asSet();

    directNotifyEmails.removeAll(slackDirectMessageConfig.getBlacklistedUserEmails());

    // If the white list is empty we send to author/committer
    // else we only send to the whitelisted members
    if (slackDirectMessageConfig.getWhitelistedUserEmails().isEmpty()) {
      return directNotifyEmails;
    } else {
      return Sets.intersection(directNotifyEmails, slackDirectMessageConfig.getWhitelistedUserEmails());
    }
  }

  private boolean shouldSendMessage(RepositoryBuild build, Set<String> userEmailsToSendMessagesTo) {

    if (!SLACK_WORTHY_FAILING_STATES.contains(build.getState())) {
      LOG.info("Build {} with state {} not in {} not sending message", build.getId().get(), build.getState(), SLACK_WORTHY_FAILING_STATES);
    }

    // Do not send notifications for builds of branches that are explicitly ignored
    String branchName = branchService.get(build.getBranchId()).get().getBranch();
    if (slackDirectMessageConfig.getIgnoredBranches().contains(branchName)) {
      LOG.info("Not sending messages for build {} because the branch is in the list of ignored branches {}", build, slackDirectMessageConfig.getIgnoredBranches());
      return false;
    }


    if (build.getBuildMetadata().getTriggeringEvent() != BuildMetadata.TriggeringEvent.PUSH) {
      LOG.info("Build {} was not a push build. Not sending messages directly to users for non-push triggered builds", build.getId().get());
      return false;
    }

    if (userEmailsToSendMessagesTo.isEmpty()) {
      LOG.info("Build {} had no users to send messages to, not sending message", build.getId().get());
      return false;
    }

    return true;
  }
}
