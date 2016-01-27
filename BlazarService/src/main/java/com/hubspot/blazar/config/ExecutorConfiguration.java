package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ExecutorConfiguration {

  @NotNull
  private final String defaultBuildUser;

  @JsonCreator
  public ExecutorConfiguration(@JsonProperty("defaultBuildUser") String defaultBuildUser) {
    this.defaultBuildUser = defaultBuildUser;
  }

  public String getDefaultBuildUser() {
    return defaultBuildUser;
  }
}
