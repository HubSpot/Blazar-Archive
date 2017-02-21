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
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.util.BlazarSlackClient;
import com.hubspot.blazar.util.SlackMessageBuildingUtils;

/**
 * Direct messages slack users about the state of builds caused by their pushes
 */
public class SlackDmNotificationVisitor implements RepositoryBuildVisitor {
  // Cancelled requires a user action - likely the user who would be slacked anyway so it is not worth messaging about
  private static final Set<RepositoryBuild.State> SLACK_WORTHY_FAILING_STATES = ImmutableSet.of(FAILED, UNSTABLE);
  private static final Logger LOG = LoggerFactory.getLogger(SlackDmNotificationVisitor.class);
  private final BlazarSlackConfiguration blazarSlackConfig;

  private final SlackMessageBuildingUtils slackMessageBuildingUtils;
  private final BlazarSlackClient blazarSlackClient;

  @Inject
  public SlackDmNotificationVisitor(BlazarConfiguration blazarConfiguration,
                                    SlackMessageBuildingUtils slackMessageBuildingUtils,
                                    BlazarSlackClient blazarSlackClient) {
    this.slackMessageBuildingUtils = slackMessageBuildingUtils;
    this.blazarSlackClient = blazarSlackClient;
    this.blazarSlackConfig = blazarConfiguration.getSlackConfiguration().get();
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    Set<String> userEmailsToSendMessagesTo = getOptedInUserEmailsForBuild(build);
    boolean shouldSendMessage = shouldSendMessage(build, userEmailsToSendMessagesTo);
    if (shouldSendMessage) {
      for (String email : userEmailsToSendMessagesTo) {
        String message = "A build started by your code push was not successful";
        blazarSlackClient.sendMessageToUser(email, message, slackMessageBuildingUtils.buildSlackAttachment(build));
      }
    }
  }

  private Set<String> getOptedInUserEmailsForBuild(RepositoryBuild build) {
    if (build.getCommitInfo().isPresent()) {
      LOG.info("No commit info present cannot determine user to slack");
      return Collections.emptySet();
    }
    String authorEmail = build.getCommitInfo().get().getCurrent().getAuthor().getEmail();
    String committerEmail = build.getCommitInfo().get().getCurrent().getAuthor().getEmail();

    // If the white list is empty we send to whoever pushed
    // Else we ensure they're also in the whitelist before sending messages
    if (blazarSlackConfig.getImWhitelist().isEmpty()) {
      return ImmutableSet.of(authorEmail, committerEmail);
    } else {
      return Sets.intersection(ImmutableSet.of(authorEmail, committerEmail), blazarSlackConfig.getImWhitelist());
    }
  }

  private boolean shouldSendMessage(RepositoryBuild build, Set<String> userEmailsToSendMessagesTo) {
    if (userEmailsToSendMessagesTo.isEmpty()) {
      return false;
    }

    boolean wasPush = build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH;
    boolean wasBranchCreation = build.getBuildTrigger().getType() == BuildTrigger.Type.BRANCH_CREATION;
    if (!(wasPush || wasBranchCreation)) {
      LOG.info("Not sending messages for triggers other than push or branch creation at this time.");
      return false;
    }

    if (!SLACK_WORTHY_FAILING_STATES.contains(build.getState())) {
      LOG.info("Build {} in state {} does not merit direct-slack-message", build.getId().get(), build.getState());
      return false;
    }
    return true;
  }
}
