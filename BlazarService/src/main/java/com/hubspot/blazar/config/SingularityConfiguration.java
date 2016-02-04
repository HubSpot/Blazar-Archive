package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.hubspot.singularity.SingularityClientCredentials;

import javax.validation.constraints.NotNull;

public class SingularityConfiguration {

  @NotNull
  private final String host;
  @NotNull
  private final String request;
  private final Optional<String> path;
  private final Optional<SingularityClientCredentials> credentials;

  @JsonCreator
  public SingularityConfiguration(@JsonProperty("host") String host,
                                  @JsonProperty("request") String request,
                                  @JsonProperty("path") Optional<String> path,
                                  @JsonProperty("credentials") Optional<SingularityClientCredentials> credentials) {
    this.host = host;
    this.request = request;
    this.path = trimSlashes(path); // singularity client adds leading and trailing slashes
    this.credentials = credentials;
  }

  public String getHost() {
    return host;
  }

  public String getRequest() {
    return request;
  }

  public Optional<String> getPath() {
    return path;
  }

  public Optional<SingularityClientCredentials> getCredentials() {
    return credentials;
  }

  private static Optional<String> trimSlashes(Optional<String> s) {
    if (s.isPresent()) {
      return Optional.of(CharMatcher.is('/').trimFrom(s.get()));
    } else {
      return s;
    }
  }
}
