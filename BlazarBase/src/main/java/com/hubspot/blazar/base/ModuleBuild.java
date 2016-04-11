package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.rosetta.annotations.StoredAsJson;

public class ModuleBuild {
  public enum SimpleState { WAITING, RUNNING, COMPLETE }

  public enum State {
    QUEUED(SimpleState.WAITING),
    WAITING_FOR_UPSTREAM_BUILD(SimpleState.WAITING),
    LAUNCHING(SimpleState.RUNNING),
    IN_PROGRESS(SimpleState.RUNNING),
    SUCCEEDED(SimpleState.COMPLETE),
    CANCELLED(SimpleState.COMPLETE),
    FAILED(SimpleState.COMPLETE),
    SKIPPED(SimpleState.COMPLETE);

    private final SimpleState simpleState;

    State(SimpleState simpleState) {
      this.simpleState = simpleState;
    }

    public boolean isWaiting() {
      return simpleState == SimpleState.WAITING;
    }

    public boolean isRunning() {
      return simpleState == SimpleState.RUNNING;
    }

    public boolean isComplete() {
      return simpleState == SimpleState.COMPLETE;
    }

    public SimpleState getSimpleState() {
      return simpleState;
    }
  }

  private final Optional<Long> id;
  private final long repoBuildId;
  private final int moduleId;
  private final int buildNumber;
  private final State state;
  private final Optional<Long> startTimestamp;
  private final Optional<Long> endTimestamp;
  private final Optional<String> taskId;
  @StoredAsJson
  private final Optional<BuildConfig> buildConfig;
  @StoredAsJson
  private final Optional<BuildConfig> resolvedConfig;

  @JsonCreator
  public ModuleBuild(@JsonProperty("id") Optional<Long> id,
                     @JsonProperty("repoBuildId") long repoBuildId,
                     @JsonProperty("moduleId") int moduleId,
                     @JsonProperty("buildNumber") int buildNumber,
                     @JsonProperty("state") State state,
                     @JsonProperty("startTimestamp") Optional<Long> startTimestamp,
                     @JsonProperty("endTimestamp") Optional<Long> endTimestamp,
                     @JsonProperty("taskId") Optional<String> taskId,
                     @JsonProperty("buildConfig") Optional<BuildConfig> buildConfig,
                     @JsonProperty("resolvedConfig") Optional<BuildConfig> resolvedConfig) {
    this.id = id;
    this.repoBuildId = repoBuildId;
    this.moduleId = moduleId;
    this.buildNumber = buildNumber;
    this.state = state;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.taskId = taskId;
    this.buildConfig = buildConfig;
    this.resolvedConfig = resolvedConfig;
  }

  public static ModuleBuild queuedBuild(RepositoryBuild repositoryBuild, Module module, int buildNumber) {
    Optional<Long> absentLong = Optional.absent();
    Optional<String> absentString = Optional.absent();
    Optional<BuildConfig> absentConfig = Optional.absent();

    return new ModuleBuild(absentLong, repositoryBuild.getId().get(), module.getId().get(), buildNumber, State.QUEUED, absentLong, absentLong, absentString, absentConfig, absentConfig);
  }

  public static ModuleBuild skippedBuild(RepositoryBuild repositoryBuild, Module module, int buildNumber) {
    Optional<Long> absentLong = Optional.absent();
    Optional<String> absentString = Optional.absent();
    Optional<BuildConfig> absentConfig = Optional.absent();

    return new ModuleBuild(absentLong, repositoryBuild.getId().get(), module.getId().get(), buildNumber, State.SKIPPED, absentLong, absentLong, absentString, absentConfig, absentConfig);
  }

  public Optional<Long> getId() {
    return id;
  }

  public long getRepoBuildId() {
    return repoBuildId;
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

  @JsonIgnore
  public Optional<String> getRunId() {
    return id.isPresent() ? Optional.of(String.valueOf(id.get())) : Optional.<String>absent();
  }

  public Optional<String> getTaskId() {
    return taskId;
  }

  public Optional<BuildConfig> getBuildConfig() {
    return buildConfig;
  }

  public Optional<BuildConfig> getResolvedConfig() {
    return resolvedConfig;
  }

  public ModuleBuild withId(long id) {
    return new ModuleBuild(Optional.of(id), repoBuildId, moduleId, buildNumber, state, startTimestamp, endTimestamp, taskId, buildConfig, resolvedConfig);
  }

  public ModuleBuild withState(State state) {
    return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, startTimestamp, endTimestamp, taskId, buildConfig, resolvedConfig);
  }

  public ModuleBuild withStartTimestamp(long startTimestamp) {
    return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, Optional.of(startTimestamp), endTimestamp, taskId, buildConfig, resolvedConfig);
  }

  public ModuleBuild withEndTimestamp(long endTimestamp) {
    return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, startTimestamp, Optional.of(endTimestamp), taskId, buildConfig, resolvedConfig);
  }

  public ModuleBuild withTaskId(String taskId) {
    return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, startTimestamp, endTimestamp, Optional.of(taskId), buildConfig, resolvedConfig);
  }

  public ModuleBuild withBuildConfig(BuildConfig buildConfig) {
    return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, startTimestamp, endTimestamp, taskId, Optional.of(buildConfig), resolvedConfig);
  }

  public ModuleBuild withResolvedConfig(BuildConfig resolvedConfig) {
    return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, startTimestamp, endTimestamp, taskId, buildConfig, Optional.of(resolvedConfig));
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
        .add("id", id)
        .add("moduleId", moduleId)
        .add("repoBuildId", repoBuildId)
        .add("state", state)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModuleBuild build = (ModuleBuild) o;
    return Objects.equals(moduleId, build.moduleId) && Objects.equals(buildNumber, build.buildNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleId, buildNumber);
  }
}
