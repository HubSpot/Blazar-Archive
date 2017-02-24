package com.hubspot.blazar.base.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubApiError {

  private final String resource;
  private final String code;
  private final String message;

  @JsonCreator
  public GitHubApiError(@JsonProperty("resource") String resource,
                        @JsonProperty("code") String code,
                        @JsonProperty("message") String message) {
    this.resource = resource;
    this.code = code;
    this.message = message;
  }

  public String getResource() {
    return resource;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
