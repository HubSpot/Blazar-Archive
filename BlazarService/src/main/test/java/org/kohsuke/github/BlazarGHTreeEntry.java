package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHTreeEntry extends GHTreeEntry {

  private final String sha;
  private BlazarGHContent content;
  private final String path;

  @JsonCreator
  public BlazarGHTreeEntry(@JsonProperty("sha") String sha,
                           @JsonProperty("content") BlazarGHContent content, // not in GHE api, we use this as the place to store the content for our files in the tests
                           @JsonProperty("path") String path) {
    this.sha = sha;
    this.content = content;
    this.path = path;
  }

  @Override
  public String getSha() {
    return this.sha;
  }

  @Override
  public String getPath() {
    return this.path;
  }

  public BlazarGHContent getContent() {
    return this.content;
  }
}
