package com.hubspot.blazar.resources;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.feedback.Feedback;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.util.SlackUtils;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;

@Path("/user-feedback")
public class UserFeedbackResource {
  private final SlackSession slackSession;
  private BlazarSlackConfiguration blazarSlackConfiguration;

  @Inject
  public UserFeedbackResource(SlackSession slackSession, BlazarConfiguration blazarConfiguration) {
    this.slackSession = slackSession;
    this.blazarSlackConfiguration = blazarConfiguration.getSlackConfiguration().get();
  }

  @POST
  public void handleFeedback(Feedback feedback) throws IOException {
    Optional<SlackChannel> channel = SlackUtils.getChannelByName(slackSession, blazarSlackConfiguration.getFeedbackRoom().get());
    if (!blazarSlackConfiguration.getFeedbackRoom().isPresent() || !channel.isPresent()) {
      throw new WebApplicationException(new Throwable("To use the feedback endpoint the feedbackRoom needs to be set in BlazarSlackConfiguration"));
    }
    String title = String.format("New Feedback from %s", feedback.getUsername());
    String fallback = String.format("New Feedback from %s: %s", feedback.getUsername(), feedback.getMessage());

    SlackAttachment attachment = new SlackAttachment(title, fallback, "", "");
    attachment.addField("Feedback:", feedback.getMessage(), false);
    attachment.setTitleLink(feedback.getPage());

    slackSession.sendMessage(channel.get(), "", attachment);
  }
}
