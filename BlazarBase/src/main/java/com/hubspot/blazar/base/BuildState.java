package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class BuildState {
  public enum Result { SUCCEEDED, IN_PROGRESS, CANCELLED, FAILED }

  private final int buildNumber;
  private final String buildLog;
  private final String commitSha;
  private final Result result;
  private final long startTime;
  private final Optional<Long> endTime;

  @JsonCreator
  public BuildState(@JsonProperty("buildNumber") int buildNumber,
                    @JsonProperty("buildLog") String buildLog,
                    @JsonProperty("commitSha") String commitSha,
                    @JsonProperty("result") Result result,
                    @JsonProperty("startTime") long startTime,
                    @JsonProperty("endTime") Optional<Long> endTime) {
    this.buildNumber = buildNumber;
    this.buildLog = buildLog;
    this.commitSha = commitSha;
    this.result = result;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public String getBuildLog() {
    return buildLog;
  }

  public String getCommitSha() {
    return commitSha;
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
