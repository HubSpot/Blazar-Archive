package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public class InterProjectBuildMapping {

  private final Optional<Long> id;
  private final long interProjectBuildId;
  private final int branchId;
  private Optional<Long> repoBuildId;
  private final int moduleId;
  private final Optional<Long> moduleBuildId;
  private InterProjectBuild.State state;

  @JsonCreator
  public InterProjectBuildMapping(@JsonProperty("id") Optional<Long> id,
                                  @JsonProperty("interProjectBuildId") long interProjectBuildId,
                                  @JsonProperty("branchid") int branchId,
                                  @JsonProperty("repoBuildId") Optional<Long> repoBuildId,
                                  @JsonProperty("moduleId") int moduleId,
                                  @JsonProperty("moduleBuildId") Optional<Long> moduleBuildId,
                                  @JsonProperty("state") InterProjectBuild.State state) {
    this.id = id;
    this.interProjectBuildId = interProjectBuildId;
    this.branchId = branchId;
    this.repoBuildId = repoBuildId;
    this.moduleId = moduleId;
    this.moduleBuildId = moduleBuildId;
    this.state = state;
  }

  public static InterProjectBuildMapping makeNewMapping(long interProjectBuildId, int branchId, Optional<Long> repoBuildId, int moduleId) {
    return new InterProjectBuildMapping(Optional.<Long>absent(), interProjectBuildId, branchId, repoBuildId, moduleId, Optional.<Long>absent(), InterProjectBuild.State.QUEUED);
  }

  public InterProjectBuildMapping withModuleBuildId(Long moduleBuildId) {
    return new InterProjectBuildMapping(id, interProjectBuildId, branchId, repoBuildId, moduleId, Optional.of(moduleBuildId), state);
  }

  public InterProjectBuildMapping withModuleBuildId(InterProjectBuild.State state) {
    return new InterProjectBuildMapping(id, interProjectBuildId, branchId, repoBuildId, moduleId, moduleBuildId, state);
  }

  public Optional<Long> getId() {
    return id;
  }

  public long getInterProjectBuildId() {
    return interProjectBuildId;
  }

  public int getBranchId() {
    return branchId;
  }

  public Optional<Long> getRepoBuildId() {
    return repoBuildId;
  }

  public int getModuleId() {
    return moduleId;
  }

  public Optional<Long> getModuleBuildId() {
    return moduleBuildId;
  }

  public InterProjectBuild.State getState() {
    return state;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("interProjectBuildId", interProjectBuildId)
        .add("branchId", branchId)
        .add("repoBuildId", repoBuildId)
        .add("moduleId", moduleId)
        .add("moduleBuildId", moduleBuildId)
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

    InterProjectBuildMapping m = (InterProjectBuildMapping) o;
    return Objects.equals(id, m.id) && Objects.equals(state, m.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, interProjectBuildId, branchId, repoBuildId, moduleId, moduleBuildId, state);
  }
}
