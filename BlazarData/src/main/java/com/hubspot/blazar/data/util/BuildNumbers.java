package com.hubspot.blazar.data.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class BuildNumbers {
  private final Optional<Long> pendingBuildId;
  private final Optional<Integer> pendingBuildNumber;
  private final Optional<Long> inProgressBuildId;
  private final Optional<Integer> inProgressBuildNumber;
  private final Optional<Long> lastBuildId;
  private final Optional<Integer> lastBuildNumber;

  @JsonCreator
  public BuildNumbers(@JsonProperty("pendingBuildId") Optional<Long> pendingBuildId,
                      @JsonProperty("pendingBuildNumber") Optional<Integer> pendingBuildNumber,
                      @JsonProperty("inProgressBuildId") Optional<Long> inProgressBuildId,
                      @JsonProperty("inProgressBuildNumber") Optional<Integer> inProgressBuildNumber,
                      @JsonProperty("lastBuildId") Optional<Long> lastBuildId,
                      @JsonProperty("lastBuildNumber") Optional<Integer> lastBuildNumber) {
    this.pendingBuildId = pendingBuildId;
    this.pendingBuildNumber = pendingBuildNumber;
    this.inProgressBuildId = inProgressBuildId;
    this.inProgressBuildNumber = inProgressBuildNumber;
    this.lastBuildId = lastBuildId;
    this.lastBuildNumber = lastBuildNumber;
  }

  public int getNextBuildNumber() {
    if (getInProgressBuildNumber().isPresent()) {
      return getInProgressBuildNumber().get() + 1;
    } else if (getLastBuildNumber().isPresent()) {
      return getLastBuildNumber().get() + 1;
    } else {
      return 1;
    }
  }

  public Optional<Long> getPendingBuildId() {
    return pendingBuildId;
  }

  public Optional<Integer> getPendingBuildNumber() {
    return pendingBuildNumber;
  }

  public Optional<Long> getInProgressBuildId() {
    return inProgressBuildId;
  }

  public Optional<Integer> getInProgressBuildNumber() {
    return inProgressBuildNumber;
  }

  public Optional<Long> getLastBuildId() {
    return lastBuildId;
  }

  public Optional<Integer> getLastBuildNumber() {
    return lastBuildNumber;
  }
}
