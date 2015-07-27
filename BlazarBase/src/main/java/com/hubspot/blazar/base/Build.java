package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class Build {
  public enum State { SUCCEEDED, IN_PROGRESS, CANCELLED, FAILED }

  private final Optional<Long> id;
  private final int moduleId;
  private final int buildNumber;
  private final State state;
  private final long startTimestamp;
  private final Optional<Long> endTimestamp;
  private final String sha;
  private final String log;

  @JsonCreator
  public Build(@JsonProperty("id") Optional<Long> id,
               @JsonProperty("moduleId") int moduleId,
               @JsonProperty("buildNumber") int buildNumber,
               @JsonProperty("state") State state,
               @JsonProperty("startTimestamp") long startTimestamp,
               @JsonProperty("endTimestamp") Optional<Long> endTimestamp,
               @JsonProperty("sha") String sha,
               @JsonProperty("log") String log) {
    this.id = id;
    this.moduleId = moduleId;
    this.buildNumber = buildNumber;
    this.state = state;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.sha = sha;
    this.log = log;
  }

  public Optional<Long> getId() {
    return id;
  }

  public int getModuleId() {
    return moduleId;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public State getState() {
    return state;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }

  public Optional<Long> getEndTimestamp() {
    return endTimestamp;
  }

  public String getSha() {
    return sha;
  }

  public String getLog() {
    return log;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Build build = (Build) o;
    return Objects.equals(moduleId, build.moduleId) &&
        Objects.equals(buildNumber, build.buildNumber) &&
        Objects.equals(startTimestamp, build.startTimestamp) &&
        Objects.equals(id, build.id) &&
        Objects.equals(state, build.state) &&
        Objects.equals(endTimestamp, build.endTimestamp) &&
        Objects.equals(sha, build.sha) &&
        Objects.equals(log, build.log);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, log);
  }
}
