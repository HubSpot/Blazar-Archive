package com.hubspot.blazar.data.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class BuildNumbers {
  private final Optional<Integer> pendingBuildNumber;
  private final Optional<Integer> inProgressBuildNumber;
  private final Optional<Integer> lastBuildNumber;

  @JsonCreator
  public BuildNumbers(@JsonProperty("pendingBuildNumber") Optional<Integer> pendingBuildNumber,
                      @JsonProperty("inProgressBuildNumber") Optional<Integer> inProgressBuildNumber,
                      @JsonProperty("lastBuildNumber") Optional<Integer> lastBuildNumber) {
    this.pendingBuildNumber = pendingBuildNumber;
    this.inProgressBuildNumber = inProgressBuildNumber;
    this.lastBuildNumber = lastBuildNumber;
  }

  public Optional<Integer> getPendingBuildNumber() {
    return pendingBuildNumber;
  }

  public Optional<Integer> getInProgressBuildNumber() {
    return inProgressBuildNumber;
  }

  public Optional<Integer> getLastBuildNumber() {
    return lastBuildNumber;
  }
}
