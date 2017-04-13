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
  private BlazarSlackDirectMessageConfiguration directMessageConfiguration;
  private String username;

  /**
   * @param slackApiBaseUrl            The slack api to point at
   * @param slackApiToken              Auth token for connecting
   * @param username                   The username to post in slack as
   * @param feedbackRoom               The room to push feedback from our in-app feedback box to
   * @param directMessageConfiguration The configuration for blazar's direct-message functionality
   */
  @Inject
  public BlazarSlackConfiguration(@JsonProperty("slackApiBaseUrl") String slackApiBaseUrl,
                                  @JsonProperty("slackApiToken") String slackApiToken,
                                  @JsonProperty("username") String username,
                                  @JsonProperty("feedbackRoom") Optional<String> feedbackRoom,
                                  @JsonProperty("directMessageConfiguration") BlazarSlackDirectMessageConfiguration directMessageConfiguration) {
    this.slackApiBaseUrl = slackApiBaseUrl;
    this.slackApiToken = slackApiToken;
    this.username = username;
    this.feedbackRoom = feedbackRoom;
    this.directMessageConfiguration = directMessageConfiguration;
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

  public BlazarSlackDirectMessageConfiguration getDirectMessageConfiguration() {
    return directMessageConfiguration;
  }
}

