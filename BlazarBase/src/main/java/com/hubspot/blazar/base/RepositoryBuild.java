package com.hubspot.blazar.base;


import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    this.buildOptions = com.google.common.base.Objects.firstNonNull(buildOptions, BuildOptions.defaultOptions());
  }

  public static RepositoryBuild queuedBuild(GitInfo gitInfo, BuildTrigger trigger, int buildNumber, BuildOptions buildOptions) {
    Optional<Long> absentLong = Optional.absent();
    Optional<String> absentString = Optional.absent();
    Optional<CommitInfo> commitInfo = Optional.absent();
    Optional<DependencyGraph> dependencyGraph = Optional.absent();

    return new RepositoryBuild(absentLong, gitInfo.getId().get(), buildNumber, State.QUEUED, trigger, absentLong, absentLong, absentString, commitInfo, dependencyGraph, buildOptions);
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

  public RepositoryBuild withId(long id) {
    return new RepositoryBuild(Optional.of(id), branchId, buildNumber, state, buildTrigger, startTimestamp, endTimestamp, sha, commitInfo, dependencyGraph, buildOptions);
  }

  public RepositoryBuild withState(State state) {
    return new RepositoryBuild(id, branchId, buildNumber, state, buildTrigger, startTimestamp, endTimestamp, sha, commitInfo, dependencyGraph, buildOptions);
  }

  public RepositoryBuild withStartTimestamp(long startTimestamp) {
    return new RepositoryBuild(id, branchId, buildNumber, state, buildTrigger, Optional.of(startTimestamp), endTimestamp, sha, commitInfo, dependencyGraph, buildOptions);
  }

  public RepositoryBuild withEndTimestamp(long endTimestamp) {
    return new RepositoryBuild(id, branchId, buildNumber, state, buildTrigger, startTimestamp, Optional.of(endTimestamp), sha, commitInfo, dependencyGraph, buildOptions);
  }

  public RepositoryBuild withCommitInfo(CommitInfo commitInfo) {
    Optional<String> sha = Optional.of(commitInfo.getCurrent().getId());
    return new RepositoryBuild(id, branchId, buildNumber, state, buildTrigger, startTimestamp, endTimestamp, sha, Optional.of(commitInfo), dependencyGraph, buildOptions);
  }

  public RepositoryBuild withDependencyGraph(DependencyGraph dependencyGraph) {
    return new RepositoryBuild(id, branchId, buildNumber, state, buildTrigger, startTimestamp, endTimestamp, sha, commitInfo, Optional.of(dependencyGraph), buildOptions);
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
}
