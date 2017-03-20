package com.hubspot.blazar.resources;

import java.io.IOException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.feedback.Feedback;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.util.BlazarSlackClient;
import com.ullink.slack.simpleslackapi.SlackAttachment;

@Path("/user-feedback")
public class UserFeedbackResource {
  private final BlazarSlackClient blazarSlackClient;
  private final Optional<String> feedbackRoom;

  @Inject
  public UserFeedbackResource(BlazarSlackClient blazarSlackClient, BlazarConfiguration blazarConfiguration) {
    this.blazarSlackClient = blazarSlackClient;
    this.feedbackRoom = blazarConfiguration.getSlackConfiguration().get().getFeedbackRoom();
  }

  @POST
  public void handleFeedback(Feedback feedback) throws IOException {
    if (!feedbackRoom.isPresent()) {
      throw new BadRequestException("No feedback room is configured, add one to the Blazar Slack configuration.");
    }

    String title = String.format("New Feedback from %s", feedback.getUsername());
    String fallback = String.format("New Feedback from %s: %s", feedback.getUsername(), feedback.getMessage());

    SlackAttachment attachment = new SlackAttachment(title, fallback, "", "");
    attachment.addField("Feedback:", feedback.getMessage(), false);
    attachment.setTitleLink(feedback.getPage());
    blazarSlackClient.sendMessageToChannel(feedbackRoom.get(), "", attachment);
  }
}
