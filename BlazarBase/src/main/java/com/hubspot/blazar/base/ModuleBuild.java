package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
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

    public boolean isFailed() {
      return equals(CANCELLED) ||
          equals(FAILED);
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
    return newBuilder(repositoryBuild.getId().get(), module.getId().get(), buildNumber, State.QUEUED).build();
  }

  public static ModuleBuild skippedBuild(RepositoryBuild repositoryBuild, Module module, int buildNumber) {
    return newBuilder(repositoryBuild.getId().get(), module.getId().get(), buildNumber, State.SKIPPED).build();
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
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

  public Builder toBuilder() {
    return new Builder(repoBuildId, moduleId, buildNumber, state)
        .setId(id)
        .setStartTimestamp(startTimestamp)
        .setEndTimestamp(endTimestamp)
        .setTaskId(taskId)
        .setBuildConfig(buildConfig)
        .setResolvedConfig(resolvedConfig);
  }


  public static Builder newBuilder(long repoBuildId, int moduleId, int buildNumber, State intialState) {
      return new Builder(repoBuildId, moduleId, buildNumber, intialState);
  }

  public static class Builder {

    private Optional<Long> id = Optional.absent();
    private long repoBuildId;
    private int moduleId;
    private int buildNumber;
    private State state;
    private Optional<Long> startTimestamp = Optional.absent();
    private Optional<Long> endTimestamp = Optional.absent();
    private Optional<String> taskId = Optional.absent();
    private Optional<BuildConfig> buildConfig = Optional.absent();
    private Optional<BuildConfig> resolvedConfig = Optional.absent();

    public Builder(long repoBuildId, int moduleId, int buildNumber, State intialState) {
      this.repoBuildId = repoBuildId;
      this.moduleId = moduleId;
      this.buildNumber = buildNumber;
      this.state = intialState;
    }

    public Builder setId(Optional<Long> id) {
      this.id = id;
      return this;
    }

    public Builder setRepoBuildId(long repoBuildId) {
      this.repoBuildId = repoBuildId;
      return this;
    }

    public Builder setModuleId(int moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    public Builder setBuildNumber(int buildNumber) {
      this.buildNumber = buildNumber;
      return this;
    }

    public Builder setState(State state) {
      this.state = state;
      return this;
    }

    public Builder setStartTimestamp(Optional<Long> startTimestamp) {
      this.startTimestamp = startTimestamp;
      return this;
    }

    public Builder setEndTimestamp(Optional<Long> endTimestamp) {
      this.endTimestamp = endTimestamp;
      return this;
    }

    public Builder setTaskId(Optional<String> taskId) {
      this.taskId = taskId;
      return this;
    }

    public Builder setBuildConfig(Optional<BuildConfig> buildConfig) {
      this.buildConfig = buildConfig;
      return this;
    }

    public Builder setResolvedConfig(Optional<BuildConfig> resolvedConfig) {
      this.resolvedConfig = resolvedConfig;
      return this;
    }

    public ModuleBuild build() {
      return new ModuleBuild(id, repoBuildId, moduleId, buildNumber, state, startTimestamp, endTimestamp, taskId, buildConfig, resolvedConfig);
    }
  }
}
