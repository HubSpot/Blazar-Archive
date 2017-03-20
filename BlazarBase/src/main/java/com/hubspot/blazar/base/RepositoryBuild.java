package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.hubspot.rosetta.annotations.StoredAsJson;

public class RepositoryBuild {
  public enum State {
    QUEUED(false), LAUNCHING(false), IN_PROGRESS(false), SUCCEEDED(true), CANCELLED(true), FAILED(true), UNSTABLE(true);

    private final boolean completed;

    State(boolean completed) {
      this.completed = completed;
    }

    public boolean isComplete() {
      return completed;
    }

    public boolean isFailed() {
      return equals(CANCELLED) ||
          equals(FAILED) ||
          equals(UNSTABLE);
    }
  }

  private final Optional<Long> id;
  private final int branchId;
  private final int buildNumber;
  private final State state;
  @StoredAsJson
  private final BuildTrigger buildTrigger;
  private final Optional<Long> startTimestamp;
  private final Optional<Long> endTimestamp;
  private final Optional<String> sha;
  @StoredAsJson
  private final Optional<CommitInfo> commitInfo;
  @StoredAsJson
  private final Optional<DependencyGraph> dependencyGraph;
  @StoredAsJson
  private final BuildOptions buildOptions;

  @JsonCreator
  public RepositoryBuild(@JsonProperty("id") Optional<Long> id,
                         @JsonProperty("branchId") int branchId,
                         @JsonProperty("buildNumber") int buildNumber,
                         @JsonProperty("state") State state,
                         @JsonProperty("buildTrigger") BuildTrigger buildTrigger,
                         @JsonProperty("startTimestamp") Optional<Long> startTimestamp,
                         @JsonProperty("endTimestamp") Optional<Long> endTimestamp,
                         @JsonProperty("sha") Optional<String> sha,
                         @JsonProperty("commitInfo") Optional<CommitInfo> commitInfo,
                         @JsonProperty("dependencyGraph") Optional<DependencyGraph> dependencyGraph,
                         @JsonProperty("buildOptions") BuildOptions buildOptions) {
    this.id = id;
    this.branchId = branchId;
    this.buildNumber = buildNumber;
    this.state = state;
    this.buildTrigger = buildTrigger;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.sha = sha;
    this.commitInfo = commitInfo;
    this.dependencyGraph = dependencyGraph;
    this.buildOptions = MoreObjects.firstNonNull(buildOptions, BuildOptions.defaultOptions());
  }

  public static RepositoryBuild queuedBuild(GitInfo gitInfo, BuildTrigger trigger, int buildNumber, BuildOptions buildOptions) {
    return newBuilder(gitInfo.getId().get(), buildNumber, State.QUEUED, trigger, buildOptions).build();
  }

  public Optional<Long> getId() {
    return id;
  }

  public int getBranchId() {
    return branchId;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public State getState() {
    return state;
  }

  public BuildTrigger getBuildTrigger() {
    return buildTrigger;
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

  public Optional<DependencyGraph> getDependencyGraph() {
    return dependencyGraph;
  }

  public BuildOptions getBuildOptions() {
    return buildOptions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RepositoryBuild build = (RepositoryBuild) o;
    return Objects.equals(branchId, build.branchId) && Objects.equals(buildNumber, build.buildNumber);
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
        .add("id", id)
        .add("branchId", branchId)
        .add("buildNumber", buildNumber)
        .add("state", state).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(branchId, buildNumber);
  }

  public Builder toBuilder() {
    return new Builder(branchId, buildNumber, state, buildTrigger, buildOptions)
        .setId(id)
        .setStartTimestamp(startTimestamp)
        .setEndTimestamp(endTimestamp)
        .setSha(sha)
        .setCommitInfo(commitInfo)
        .setDependencyGraph(dependencyGraph);
  }

  public static Builder newBuilder(int branchId, int buildNumber, State state, BuildTrigger buildTrigger, BuildOptions buildOptions) {
    return new Builder(branchId, buildNumber, state, buildTrigger, buildOptions);
  }

  public static class Builder {
    private Optional<Long> id = Optional.absent();
    private int branchId;
    private int buildNumber;
    private State state;
    private BuildTrigger buildTrigger;
    private Optional<Long> startTimestamp = Optional.absent();
    private Optional<Long> endTimestamp = Optional.absent();
    private Optional<String> sha = Optional.absent();
    private Optional<CommitInfo> commitInfo = Optional.absent();
    private Optional<DependencyGraph> dependencyGraph = Optional.absent();
    private BuildOptions buildOptions;


    Builder(int branchId, int buildNumber, State state, BuildTrigger buildTrigger, BuildOptions buildOptions) {
      this.branchId = branchId;
      this.buildNumber = buildNumber;
      this.state = state;
      this.buildTrigger = buildTrigger;
      this.buildOptions = buildOptions;
    }

    public Builder setId(Optional<Long> id) {
      this.id = id;
      return this;
    }

    public Builder setBranchId(int branchId) {
      this.branchId = branchId;
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

    public Builder setBuildTrigger(BuildTrigger buildTrigger) {
      this.buildTrigger = buildTrigger;
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

    public Builder setSha(Optional<String> sha) {
      this.sha = sha;
      return this;
    }

    public Builder setCommitInfo(Optional<CommitInfo> commitInfo) {
      this.commitInfo = commitInfo;
      return this;
    }

    public Builder setDependencyGraph(Optional<DependencyGraph> dependencyGraph) {
      this.dependencyGraph = dependencyGraph;
      return this;
    }

    public Builder setBuildOptions(BuildOptions buildOptions) {
      this.buildOptions = buildOptions;
      return this;
    }

    public RepositoryBuild build() {
      return new RepositoryBuild(id, branchId, buildNumber, state, buildTrigger, startTimestamp, endTimestamp, sha, commitInfo, dependencyGraph, buildOptions);
    }
  }
}
