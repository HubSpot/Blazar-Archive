package com.hubspot.blazar.resources;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.hubspot.blazar.base.feedback.Feedback;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SlackConfiguration;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;

@Path("/slack")
public class SlackResource {

  private final SlackConfiguration slackConfiguration;
  private DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;
  private DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;


  @Inject
  public SlackResource(BlazarConfiguration configuration) {
    this.slackConfiguration = configuration.getSlackConfiguration();
  }

  @POST
  @Path("/")
  public void sendMessage(Feedback feedback) throws IOException {
    if (slackConfiguration.getToken().isEmpty() || slackConfiguration.getRoom().isEmpty()) {
      throw new IllegalArgumentException("Slack is not configured properly");
    }

    long adjustedTimestamp = (feedback.getTimestamp()/1000)*1000;

    ZonedDateTime ESTMillis = ZonedDateTime.ofInstant(Instant.ofEpochMilli(adjustedTimestamp), ZoneId.of("America/New_York"));
    SlackAttachment.Field date = new SlackAttachment.Field("Date", String.format("%s %s", ESTMillis.format(dateFormat), ESTMillis.format(timeFormat)), true);

    Slack api = new Slack(slackConfiguration.getSlackWebhookUrl()).displayName("Blazar").icon(":fire:");

    SlackAttachment attachment = new SlackAttachment(feedback.getMessage()).addField(date);
    if (feedback.getOther().isPresent()) {
      attachment = attachment.addField(new SlackAttachment.Field("OtherData", feedback.getOther().get(), true));
    }

    attachment = attachment
        .title(String.format("New Feedback from %s", feedback.getUsername()), feedback.getPage())
        .fallback(String.format("New Feedback from %s: %s", feedback.getUsername(), feedback.getMessage()));

    api.push(ImmutableList.of(attachment));
  }
}
