package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class GitInfo {
  private final Optional<Long> id;
  private final String host;
  private final String organization;
  private final String repository;
  private final long repositoryId;
  private final String branch;
  private final boolean active;

  @JsonCreator
  public GitInfo(@JsonProperty("id") Optional<Long> id,
                 @JsonProperty("host") String host,
                 @JsonProperty("organization") String organization,
                 @JsonProperty("repository") String repository,
                 @JsonProperty("repositoryId") long repositoryId,
                 @JsonProperty("branch") String branch,
                 @JsonProperty("active") boolean active) {
    this.id = id;
    this.host = host;
    this.organization = organization;
    this.repository = repository;
    this.repositoryId = repositoryId;
    this.branch = branch;
    this.active = active;
  }

  public Optional<Long> getId() {
    return id;
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

  public long getRepositoryId() {
    return repositoryId;
  }

  public String getBranch() {
    return branch;
  }

  public boolean isActive() {
    return active;
  }

  @JsonIgnore
  public String getFullRepositoryName() {
    return getOrganization() + '/' + getRepository();
  }

  public GitInfo withId(long id) {
    return new GitInfo(Optional.of(id), host, organization, repository, repositoryId, branch, active);
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
    return Objects.equals(repositoryId, gitInfo.repositoryId) &&
        Objects.equals(active, gitInfo.active) &&
        Objects.equals(id, gitInfo.id) &&
        Objects.equals(host, gitInfo.host) &&
        Objects.equals(organization, gitInfo.organization) &&
        Objects.equals(repository, gitInfo.repository) &&
        Objects.equals(branch, gitInfo.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, host, organization, repository, repositoryId, branch, active);
  }
}
