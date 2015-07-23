package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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

  @JsonIgnore
  public String getFullRepositoryName() {
    return getOrganization() + '/' + getRepository();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GitInfo gitInfo = (GitInfo) o;

    return Objects.equals(host, gitInfo.host) &&
        Objects.equals(organization, gitInfo.organization) &&
        Objects.equals(repository, gitInfo.repository) &&
        Objects.equals(branch, gitInfo.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, organization, repository, branch);
  }
}
