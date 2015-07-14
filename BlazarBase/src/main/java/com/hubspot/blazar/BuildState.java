package com.hubspot.blazar;

import java.util.Optional;

public class BuildState {
  public enum Result { SUCCEEDED, IN_PROGRESS, FAILED }

  private final int buildNumber;
  private final Result result;
  private final long startTime;
  private final Optional<Long> endTime;

  public BuildState(int buildNumber, Result result, long startTime, Optional<Long> endTime) {
    this.buildNumber = buildNumber;
    this.result = result;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public Result getResult() {
    return result;
  }

  public long getStartTime() {
    return startTime;
  }

  public Optional<Long> getEndTime() {
    return endTime;
  }
}
