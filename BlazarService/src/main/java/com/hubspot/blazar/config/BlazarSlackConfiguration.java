package com.hubspot.blazar.config;


import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.istack.internal.NotNull;

public class BlazarSlackConfiguration {

  @NotNull
  @NotEmpty
  private final String slackApiBaseUrl;
  private final String slackApiToken;
  private final Optional<String> feedbackRoom;
  private String username;

  @Inject
  public BlazarSlackConfiguration(@JsonProperty("slackApiBaseUrl") String slackApiBaseUrl,
                                  @JsonProperty("slackApiToken") String slackApiToken,
                                  @JsonProperty("username") String username,
                                  @JsonProperty("feedbackRoom") Optional<String> feedbackRoom) {
    this.slackApiBaseUrl = slackApiBaseUrl;
    this.slackApiToken = slackApiToken;
    this.username = username;
    this.feedbackRoom = feedbackRoom;
  }

  public String getSlackApiBaseUrl() {
    return slackApiBaseUrl;
  }

  public String getSlackApiToken() {
    return slackApiToken;
  }

  public String getUsername() {
    return username;
  }

  public Optional<String> getFeedbackRoom() {
    return feedbackRoom;
  }
}
