package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitInfo {
  private final String host;
  private final String organization;
  private final String repository;
  private final String branch;

  @JsonCreator
  public GitInfo(@JsonProperty("host") String host,
                 @JsonProperty("organization") String organization,
                 @JsonProperty("repository") String repository,
                 @JsonProperty("branch") String branch) {
    this.host = host;
    this.organization = organization;
    this.repository = repository;
    this.branch = branch;
  }

  public String getHost() {
    return host;
  }

  public String getOrganization() {
    return organization;
  }

  public String getRepository() {
    return repository;
  }

  public String getBranch() {
    return branch;
  }
}
