package com.hubspot.blazar.resources;

import static com.hubspot.blazar.guice.BlazarSlackModule.SLACK_FEEDBACK_ROOM;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hubspot.blazar.base.feedback.Feedback;
import com.hubspot.blazar.util.SlackClientWrapper;
import com.ullink.slack.simpleslackapi.SlackAttachment;

@Path("/user-feedback")
public class UserFeedbackResource {
  private final SlackClientWrapper slackClientWrapper;
  private final String feedbackRoom;

  @Inject
  public UserFeedbackResource(SlackClientWrapper slackClientWrapper, @Named(SLACK_FEEDBACK_ROOM) String feedbackRoom) {
    this.slackClientWrapper = slackClientWrapper;
    this.feedbackRoom = feedbackRoom;
  }

  @POST
  public void handleFeedback(Feedback feedback) throws IOException {
    String title = String.format("New Feedback from %s", feedback.getUsername());
    String fallback = String.format("New Feedback from %s: %s", feedback.getUsername(), feedback.getMessage());

    SlackAttachment attachment = new SlackAttachment(title, fallback, "", "");
    attachment.addField("Feedback:", feedback.getMessage(), false);
    attachment.setTitleLink(feedback.getPage());
    slackClientWrapper.sendMessageToChannelByName(feedbackRoom, "", attachment);
  }
}
