package com.hubspot.blazar.config;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.hubspot.singularity.SingularityClientCredentials;

public class SingularityConfiguration {

  @NotNull
  private final String host;
  @NotNull
  private final String request;
  private final Optional<String> path;
  private final Optional<SingularityClientCredentials> credentials;
  private final int slaveHttpPort;

  @JsonCreator
  public SingularityConfiguration(@JsonProperty("host") String host,
                                  @JsonProperty("request") String request,
                                  @JsonProperty("path") Optional<String> path,
                                  @JsonProperty("credentials") Optional<SingularityClientCredentials> credentials,
                                  @JsonProperty("slaveHttpPort") int slaveHttpPort) {
    this.host = host;
    this.request = request;
    this.path = trimSlashes(path); // singularity client adds leading and trailing slashes
    this.credentials = credentials;
    this.slaveHttpPort = slaveHttpPort;
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

  public int getSlaveHttpPort() {
    return slaveHttpPort;
  }

  private static Optional<String> trimSlashes(Optional<String> s) {
    if (s.isPresent()) {
      return Optional.of(CharMatcher.is('/').trimFrom(s.get()));
    } else {
      return s;
    }
  }
}
