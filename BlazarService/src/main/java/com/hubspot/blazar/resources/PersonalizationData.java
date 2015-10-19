package com.hubspot.blazar.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubspot.blazar.base.GitInfo;

import java.util.Set;

public class PersonalizationData {

  private final Set<GitInfo> repositories;

  @JsonCreator
  PersonalizationData(@JsonProperty("repositories") Set<GitInfo> repositories) {
    this.repositories = repositories;
  }

  public Set<GitInfo> getRepositories() {
    return repositories;
  }
}

