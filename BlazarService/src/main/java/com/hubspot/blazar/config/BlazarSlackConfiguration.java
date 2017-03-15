package com.hubspot.blazar.config;

import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.istack.internal.NotNull;

public class BlazarSlackConfiguration {

  @NotNull
  @NotEmpty
  private final String slackApiBaseUrl;
  private final String slackApiToken;
  private final Optional<String> feedbackRoom;
  private final Set<String> imBlacklist;
  private Set<String> imWhitelist;
  private String username;

  @Inject
  public BlazarSlackConfiguration(@JsonProperty("slackApiBaseUrl") String slackApiBaseUrl,
                                  @JsonProperty("slackApiToken") String slackApiToken,
                                  @JsonProperty("username") String username,
                                  @JsonProperty("feedbackRoom") Optional<String> feedbackRoom,
                                  @JsonProperty("imWhitelist") Set<String> imWhitelist,
                                  @JsonProperty("imBlacklist") Set<String> imBlacklist) {
    this.slackApiBaseUrl = slackApiBaseUrl;
    this.slackApiToken = slackApiToken;
    this.username = username;
    this.feedbackRoom = feedbackRoom;
    this.imWhitelist = MoreObjects.firstNonNull(imWhitelist, Collections.emptySet());
    this.imBlacklist = MoreObjects.firstNonNull(imBlacklist, Collections.emptySet());
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

  public Set<String> getImWhitelist() {
    return imWhitelist;
  }

  public Set<String> getImBlacklist() {
    return imBlacklist;
  }
}
