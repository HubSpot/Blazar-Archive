package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.rosetta.annotations.StoredAsJson;

import java.util.Objects;

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
  private final BuildTrigger trigger;
  private final Optional<Long> startTimestamp;
  private final Optional<Long> endTimestamp;
  private final Optional<String> sha;
  @StoredAsJson
  private final Optional<CommitInfo> commitInfo;
  @StoredAsJson
  private final Optional<DependencyGraph> dependencyGraph;

  @JsonCreator
  public RepositoryBuild(@JsonProperty("id") Optional<Long> id,
                         @JsonProperty("branchId") int branchId,
                         @JsonProperty("buildNumber") int buildNumber,
                         @JsonProperty("state") State state,
                         @JsonProperty("trigger") BuildTrigger trigger,
                         @JsonProperty("startTimestamp") Optional<Long> startTimestamp,
                         @JsonProperty("endTimestamp") Optional<Long> endTimestamp,
                         @JsonProperty("sha") Optional<String> sha,
                         @JsonProperty("commitInfo") Optional<CommitInfo> commitInfo,
                         @JsonProperty("dependencyGraph") Optional<DependencyGraph> dependencyGraph) {
    this.id = id;
    this.branchId = branchId;
    this.buildNumber = buildNumber;
    this.state = state;
    this.trigger = trigger;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.sha = sha;
    this.commitInfo = commitInfo;
    this.dependencyGraph = dependencyGraph;
  }

  public static RepositoryBuild queuedBuild(GitInfo gitInfo, BuildTrigger trigger, int buildNumber) {
    Optional<Long> absentLong = Optional.absent();
    Optional<String> absentString = Optional.absent();
    Optional<CommitInfo> commitInfo = Optional.absent();
    Optional<DependencyGraph> dependencyGraph = Optional.absent();

    return new RepositoryBuild(absentLong, gitInfo.getId().get(), buildNumber, State.QUEUED, trigger, absentLong, absentLong, absentString, commitInfo, dependencyGraph);
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

  public BuildTrigger getTrigger() {
    return trigger;
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

  public RepositoryBuild withId(long id) {
    return new RepositoryBuild(Optional.of(id), branchId, buildNumber, state, trigger, startTimestamp, endTimestamp, sha, commitInfo, dependencyGraph);
  }

  public RepositoryBuild withState(State state) {
    return new RepositoryBuild(id, branchId, buildNumber, state, trigger, startTimestamp, endTimestamp, sha, commitInfo, dependencyGraph);
  }

  public RepositoryBuild withStartTimestamp(long startTimestamp) {
    return new RepositoryBuild(id, branchId, buildNumber, state, trigger, Optional.of(startTimestamp), endTimestamp, sha, commitInfo, dependencyGraph);
  }

  public RepositoryBuild withEndTimestamp(long endTimestamp) {
    return new RepositoryBuild(id, branchId, buildNumber, state, trigger, startTimestamp, Optional.of(endTimestamp), sha, commitInfo, dependencyGraph);
  }

  public RepositoryBuild withCommitInfo(CommitInfo commitInfo) {
    Optional<String> sha = Optional.of(commitInfo.getCurrent().getId());
    return new RepositoryBuild(id, branchId, buildNumber, state, trigger, startTimestamp, endTimestamp, sha, Optional.of(commitInfo), dependencyGraph);
  }

  public RepositoryBuild withDependencyGraph(DependencyGraph dependencyGraph) {
    return new RepositoryBuild(id, branchId, buildNumber, state, trigger, startTimestamp, endTimestamp, sha, commitInfo, Optional.of(dependencyGraph));
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
  public int hashCode() {
    return Objects.hash(branchId, buildNumber);
  }
}
