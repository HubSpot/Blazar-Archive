package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class GitHubConfiguration {
  private final Optional<String> endpoint;
  private final Optional<String> user;
  private final Optional<String> password;
  private final Optional<String> oauthToken;

  @JsonCreator
  public GitHubConfiguration(@JsonProperty("endpoint") Optional<String> endpoint,
                             @JsonProperty("user") Optional<String> user,
                             @JsonProperty("password") Optional<String> password,
                             @JsonProperty("oauthToken") Optional<String> oauthToken) {
    this.endpoint = endpoint;
    this.user = user;
    this.password = password;
    this.oauthToken = oauthToken;
  }

  public Optional<String> getEndpoint() {
    return endpoint;
  }

  public Optional<String> getUser() {
    return user;
  }

  public Optional<String> getPassword() {
    return password;
  }

  public Optional<String> getOauthToken() {
    return oauthToken;
  }
}
