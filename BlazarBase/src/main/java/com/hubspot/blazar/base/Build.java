package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.rosetta.annotations.StoredAsJson;

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
  @StoredAsJson
  private final Optional<CommitInfo> commitInfo;
  private final Optional<String> log;
  @StoredAsJson
  private final Optional<BuildConfig> buildConfig;

  @JsonCreator
  public Build(@JsonProperty("id") Optional<Long> id,
               @JsonProperty("moduleId") int moduleId,
               @JsonProperty("buildNumber") int buildNumber,
               @JsonProperty("state") State state,
               @JsonProperty("startTimestamp") Optional<Long> startTimestamp,
               @JsonProperty("endTimestamp") Optional<Long> endTimestamp,
               @JsonProperty("sha") Optional<String> sha,
               @JsonProperty("commitInfo") Optional<CommitInfo> commitInfo,
               @JsonProperty("log") Optional<String> log,
               @JsonProperty("buildConfig") Optional<BuildConfig> buildConfig) {
    this.id = id;
    this.moduleId = moduleId;
    this.buildNumber = buildNumber;
    this.state = state;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.sha = sha;
    this.commitInfo = commitInfo;
    this.log = log;
    this.buildConfig = buildConfig;
  }

  public static Build queuedBuild(Module module, int buildNumber) {
    Optional<Long> absentLong = Optional.absent();
    Optional<String> absentString = Optional.absent();
    Optional<CommitInfo> commitInfo = Optional.absent();
    Optional<BuildConfig> buildConfig = Optional.absent();

    return new Build(absentLong, module.getId().get(), buildNumber, State.QUEUED, absentLong, absentLong, absentString, commitInfo, absentString, buildConfig);
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

  public Optional<CommitInfo> getCommitInfo() {
    return commitInfo;
  }

  public Optional<String> getLog() {
    return log;
  }

  public Optional<BuildConfig> getBuildConfig() {
    return buildConfig;
  }

  public Build withId(long id) {
    return new Build(Optional.of(id), moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, commitInfo, log, buildConfig);
  }

  public Build withCommitInfo(CommitInfo commitInfo) {
    Optional<String> sha = Optional.of(commitInfo.getCurrent().getSha());
    return new Build(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, Optional.of(commitInfo), log, buildConfig);
  }

  public Build withState(State state) {
    return new Build(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, commitInfo, log, buildConfig);
  }

  public Build withStartTimestamp(long startTimestamp) {
    return new Build(id, moduleId, buildNumber, state, Optional.of(startTimestamp), endTimestamp, sha, commitInfo, log, buildConfig);
  }

  public Build withEndTimestamp(long endTimestamp) {
    return new Build(id, moduleId, buildNumber, state, startTimestamp, Optional.of(endTimestamp), sha, commitInfo, log, buildConfig);
  }

  public Build withLog(String log) {
    return new Build(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, commitInfo, Optional.of(log), buildConfig);
  }

  public Build withBuildConfig(BuildConfig buildConfig) {
    return new Build(id, moduleId, buildNumber, state, startTimestamp, endTimestamp, sha, commitInfo, log, Optional.of(buildConfig));
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
    return Objects.equals(moduleId, build.moduleId) && Objects.equals(buildNumber, build.buildNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleId, buildNumber);
  }
}
