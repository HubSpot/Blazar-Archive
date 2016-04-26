package org.kohsuke.github;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class BlazarGHContent extends GHContent {

  private final String content;

  @JsonCreator
  public BlazarGHContent (@JsonProperty("content") String content) {
    this.content = content;
  }

  @Override
  public String getContent() {
    return content;
  }

  @JsonCreator
  // lets us specify content w/o making extra yaml keys for a single field object
  public static BlazarGHContent fromString(String path) {
    try {
      return new BlazarGHContent(Resources.toString(Resources.getResource(path), Charsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
