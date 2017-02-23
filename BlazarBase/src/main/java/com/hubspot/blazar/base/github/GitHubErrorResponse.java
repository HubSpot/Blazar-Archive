package com.hubspot.blazar.base.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubErrorResponse {
  private final String message;
  private final List<GitHubApiError> errors;
  private final String documentationUrl;

  @JsonCreator
  public GitHubErrorResponse(@JsonProperty("message") String message,
                             @JsonProperty("errors") List<GitHubApiError> errors,
                             @JsonProperty("documentation_url") String documentationUrl) {
    this.message = message;
    this.errors = errors;
    this.documentationUrl = documentationUrl;
  }

  public String getMessage() {
    return message;
  }

  public List<GitHubApiError> getErrors() {
    return errors;
  }

  public String getDocumentationUrl() {
    return documentationUrl;
  }
}
