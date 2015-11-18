package com.hubspot.blazar.data.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class BuildNumbers {
  private final Optional<Integer> pendingBuildId;
  private final Optional<Integer> pendingBuildNumber;
  private final Optional<Integer> inProgressBuildId;
  private final Optional<Integer> inProgressBuildNumber;
  private final Optional<Integer> lastBuildId;
  private final Optional<Integer> lastBuildNumber;

  @JsonCreator
  public BuildNumbers(@JsonProperty("pendingBuildId") Optional<Integer> pendingBuildId,
                      @JsonProperty("pendingBuildNumber") Optional<Integer> pendingBuildNumber,
                      @JsonProperty("inProgressBuildId") Optional<Integer> inProgressBuildId,
                      @JsonProperty("inProgressBuildNumber") Optional<Integer> inProgressBuildNumber,
                      @JsonProperty("lastBuildId") Optional<Integer> lastBuildId,
                      @JsonProperty("lastBuildNumber") Optional<Integer> lastBuildNumber) {
    this.pendingBuildId = pendingBuildId;
    this.pendingBuildNumber = pendingBuildNumber;
    this.inProgressBuildId = inProgressBuildId;
    this.inProgressBuildNumber = inProgressBuildNumber;
    this.lastBuildId = lastBuildId;
    this.lastBuildNumber = lastBuildNumber;
  }

  public Optional<Integer> getPendingBuildId() {
    return pendingBuildId;
  }

  public Optional<Integer> getPendingBuildNumber() {
    return pendingBuildNumber;
  }

  public Optional<Integer> getInProgressBuildId() {
    return inProgressBuildId;
  }

  public Optional<Integer> getInProgressBuildNumber() {
    return inProgressBuildNumber;
  }

  public Optional<Integer> getLastBuildId() {
    return lastBuildId;
  }

  public Optional<Integer> getLastBuildNumber() {
    return lastBuildNumber;
  }
}
