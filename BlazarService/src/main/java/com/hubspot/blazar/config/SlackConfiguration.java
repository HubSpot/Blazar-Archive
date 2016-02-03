package com.hubspot.blazar.config;

import javax.validation.constraints.NotNull;

import in.ashwanthkumar.slack.webhook.Slack;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SlackConfiguration {

  @NotNull
  @NotEmpty
  private final String room;

  @NotNull
  @NotEmpty
  private final String url;

  private final Slack slack;

  @JsonCreator
  public SlackConfiguration(@JsonProperty("room") String room, @JsonProperty("url") String url) {
    this.room = room;
    this.url = url;
    this.slack = new Slack(url).displayName("Blazar").icon(":fire:").sendToChannel(room);
  }

  public String getRoom() {
    return room;
  }

  public String getUrl() {
    return url;
  }

  public Slack getSlack() {
    return slack;
  }
}
