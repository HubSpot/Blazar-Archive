package com.hubspot.blazar.externalservice.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackUserProfile {
  private String email;

  @JsonCreator
  public SlackUserProfile(@JsonProperty("email") String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}
