package com.hubspot.blazar.base.ConfigParts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class Buildpack {
  private final String gitUrl;
  private final String tag;

  @JsonCreator
  public Buildpack(@JsonProperty("gitUrl") String gitUrl,
                   @JsonProperty("tag") String tag) {

    this.gitUrl = gitUrl;
    this.tag = tag;
  }

  public String getGitUrl() {
    return gitUrl;
  }

  public String getTag() {
    return tag;
  }
}
