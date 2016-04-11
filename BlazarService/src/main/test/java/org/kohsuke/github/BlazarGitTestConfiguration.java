package org.kohsuke.github;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGitTestConfiguration {

  private final Map<String, List<BlazarGHRepository>> config;

  @JsonCreator
  public BlazarGitTestConfiguration(@JsonProperty("config") Map<String, List<BlazarGHRepository>> config) {
    for (Map.Entry<String, List<BlazarGHRepository>> entry : config.entrySet()) {
      String hostname = entry.getKey();
      for (BlazarGHRepository b : entry.getValue()) {
        b.setHost(hostname);
      }
    }
    this.config = config;
  }

  @JsonIgnore
  public Map<String, List<BlazarGHRepository>> getConfig() {
    return this.config;
  }
}

