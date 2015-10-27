package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SlackConfiguration {

  private final String token;
  private final String room;
  private String slackWebhookUrl;
  private static final String BASE_URL = "https://slack.com/api";
  private static final String BLAZAR_ICON_LINK = "https://static.hsappstatic.net/BlazarUI/static-1.32246/images/blazar-logo.png";

  @JsonCreator
  public SlackConfiguration(@JsonProperty("token") String token,
                            @JsonProperty("room") String room,
                            @JsonProperty("url") String slackWebhookUrl) {
    this.token = token;
    this.room = room;
    this.slackWebhookUrl = slackWebhookUrl;
  }

  public String getToken() {
    return token;
  }

  public String getRoom() {
    return room;
  }

  public String getSlackWebhookUrl() {
    return slackWebhookUrl;
  }

  public String getBaseUrl() {
    return BASE_URL;
  }

  public String getBlazarIconLink() {
    return BLAZAR_ICON_LINK;
  }
}
