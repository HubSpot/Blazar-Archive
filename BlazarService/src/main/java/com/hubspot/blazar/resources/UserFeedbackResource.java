package com.hubspot.blazar.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.hubspot.blazar.externalservice.slack.SlackAttachment;
import com.hubspot.blazar.externalservice.slack.SlackAttachmentField;
import com.hubspot.blazar.externalservice.slack.SlackMessage;
import com.hubspot.blazar.base.feedback.Feedback;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.integration.slack.SlackClient;

@Path("/user-feedback")
public class UserFeedbackResource {
  private final SlackClient slackClient;
  private BlazarSlackConfiguration blazarSlackConfiguration;

  @Inject
  public UserFeedbackResource(SlackClient slackClient) {
    this.slackClient = slackClient;
    this.blazarSlackConfiguration = slackClient.getBlazarSlackConfiguration();
  }

  @POST
  public void handleFeedback(Feedback feedback) throws IOException {
    if (!blazarSlackConfiguration.getFeedbackRoom().isPresent()) {
      throw new WebApplicationException(new Throwable("To use the feedback endpoint the feedbackRoom needs to be set in BlazarSlackConfiguration"));
    }

    List<SlackAttachmentField> fields = new ArrayList<>();
    Optional<String> absentString = Optional.absent();
    if (feedback.getOther().isPresent()) {
      SlackAttachmentField attachedField = new SlackAttachmentField("OtherData", feedback.getOther().get(), false);
      fields.add(attachedField);
    }

    Optional<String> title = Optional.of(String.format("New Feedback from %s", feedback.getUsername()));
    String fallback = String.format("New Feedback from %s: %s", feedback.getUsername(), feedback.getMessage());
    Optional<String> link = Optional.of(feedback.getPage());
    Optional<String> message = Optional.of(feedback.getMessage());

    SlackAttachment attachment = new SlackAttachment(fallback, absentString, absentString, absentString, absentString, absentString, title, link, message, fields, absentString);

    SlackMessage.Builder b = SlackMessage.newBuilder();
    b.setIcon_emoji(Optional.of(":fire:"));
    b.setChannel(blazarSlackConfiguration.getFeedbackRoom().get());
    b.setAttachments(ImmutableList.of(attachment));
    slackClient.sendMessage(b.build());
  }
}
