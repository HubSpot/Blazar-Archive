package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class InterProjectBuildMapping {

  private final Optional<Long> id;
  private final long interProjectBuildId;
  private final int repoId;
  private Optional<Long> repoBuildId;
  private final int moduleId;
  private final Optional<Long> moduleBuildId;

  @JsonCreator
  public InterProjectBuildMapping(@JsonProperty("id") Optional<Long> id,
                                  @JsonProperty("interProjectBuildId") long interProjectBuildId,
                                  @JsonProperty("repoId") int repoId,
                                  @JsonProperty("repoBuildId") Optional<Long> repoBuildId,
                                  @JsonProperty("moduleId") int moduleId,
                                  @JsonProperty("moduleBuildId") Optional<Long> moduleBuildId) {
    this.id = id;
    this.interProjectBuildId = interProjectBuildId;
    this.repoId = repoId;
    this.repoBuildId = repoBuildId;
    this.moduleId = moduleId;
    this.moduleBuildId = moduleBuildId;
  }

  public static InterProjectBuildMapping makeNewMapping(long interProjectBuildId, int repoId, Optional<Long> repoBuildId, int moduleId) {
    return new InterProjectBuildMapping(Optional.<Long>absent(), interProjectBuildId, repoId, repoBuildId, moduleId, Optional.<Long>absent());
  }

  public InterProjectBuildMapping withModuleBuildId(Long moduleBuildId) {
    return new InterProjectBuildMapping(id, interProjectBuildId, repoId, repoBuildId, moduleId, Optional.of(moduleBuildId));
  }

  public Optional<Long> getId() {
    return id;
  }

  public long getInterProjectBuildId() {
    return interProjectBuildId;
  }

  public int getRepoId() {
    return repoId;
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

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
        .add("id", id)
        .add("interProjectBuildId", interProjectBuildId)
        .add("repoId", repoId)
        .add("repoBuildId", repoBuildId)
        .add("moduleId", moduleId)
        .add("moduleBuildId", moduleBuildId)
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
    return // Objects.equals(interProjectBuildId, m.interProjectBuildId) &&
        Objects.equals(id, m.id);
        // Objects.equals(repoId, m.repoId) &&
        // Objects.equals(repoBuildId, m.repoBuildId) &&
        // Objects.equals(moduleId, m.moduleId) &&
        // Objects.equals(moduleBuildId, m.moduleBuildId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, interProjectBuildId, repoId, repoBuildId, moduleId, moduleBuildId);
  }
}
