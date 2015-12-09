package com.hubspot.blazar.resources;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.hubspot.blazar.base.feedback.Feedback;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SlackConfiguration;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;

@Path("/feedback")
public class FeedbackResource {
  private final Optional<SlackConfiguration> slackConfiguration;

  @Inject
  public FeedbackResource(BlazarConfiguration configuration) {
    this.slackConfiguration = configuration.getSlackConfiguration();
  }

  @POST
  public void handleFeedback(Feedback feedback) throws IOException {
    if (!slackConfiguration.isPresent()) {
      throw new WebApplicationException(new Throwable("Slack is not configured"));
    }

    Slack api = slackConfiguration.get().getSlack();
    SlackAttachment attachment = new SlackAttachment(feedback.getMessage());
    if (feedback.getOther().isPresent()) {
      attachment = attachment.addField(new SlackAttachment.Field("OtherData", feedback.getOther().get(), false));
    }
    attachment = attachment
        .title(String.format("New Feedback from %s", feedback.getUsername()), feedback.getPage())
        .fallback(String.format("New Feedback from %s: %s", feedback.getUsername(), feedback.getMessage()));
    api.push(ImmutableList.of(attachment));
  }
}
