package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class Build {
  public enum State {
    QUEUED(false), LAUNCHING(false), IN_PROGRESS(false), SUCCEEDED(true), CANCELLED(true), FAILED(true);

    private final boolean completed;

    State(boolean completed) {
      this.completed = completed;
    }

    public boolean isComplete() {
      return completed;
    }
  }

  private final Optional<Long> id;
  private final int moduleId;
  private final int buildNumber;
  private final State state;
  private final Optional<Long> startTimestamp;
  private final Optional<Long> endTimestamp;
  private final Optional<String> sha;
  private final Optional<String> log;

  @JsonCreator
  public Build(@JsonProperty("id") Optional<Long> id,
               @JsonProperty("moduleId") int moduleId,
               @JsonProperty("buildNumber") int buildNumber,
               @JsonProperty("state") State state,
               @JsonProperty("startTimestamp") Optional<Long> startTimestamp,
               @JsonProperty("endTimestamp") Optional<Long> endTimestamp,
               @JsonProperty("sha") Optional<String> sha,
               @JsonProperty("log") Optional<String> log) {
    this.id = id;
    this.moduleId = moduleId;
    this.buildNumber = buildNumber;
    this.state = state;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.sha = sha;
    this.log = log;
  }

  public static Build queuedBuild(Module module, int buildNumber) {
    Optional<Long> absentLong = Optional.absent();
    Optional<String> absentString = Optional.absent();

    return new Build(absentLong, module.getId().get(), buildNumber, State.QUEUED, absentLong, absentLong, absentString, absentString);
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

  public Optional<Long> getStartTimestamp() {
    return startTimestamp;
  }

  public Optional<Long> getEndTimestamp() {
    return endTimestamp;
  }

  public Optional<String> getSha() {
    return sha;
  }

  public Optional<String> getLog() {
    return log;
  }

  public Build withId(long id) {
    return new Build(Optional.of(id), moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, log);
  }

  public Build withSha(String sha) {
    return new Build(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, Optional.of(sha), log);
  }

  public Build withState(State state) {
    return new Build(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, log);
  }

  public Build withStartTimestamp(long startTimestamp) {
    return new Build(id, moduleId, buildNumber, state, Optional.of(startTimestamp), endTimestamp, sha, log);
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
