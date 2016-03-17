package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class InterProjectBuildMapping {

  private final long interProjectBuildId;
  private final long mappingId;
  private final Optional<Long> buildId;

  @JsonCreator
  public InterProjectBuildMapping(@JsonProperty("interProjectBuildId") long interProjectBuildId,
                                  @JsonProperty("mappingId") long mappingId,
                                  @JsonProperty("buildId") Optional<Long> buildId) {
    this.interProjectBuildId = interProjectBuildId;
    this.mappingId = mappingId;
    this.buildId = buildId;
  }

  public long getInterProjectBuildId() {
    return interProjectBuildId;
  }

  public long getMappingId() {
    return mappingId;
  }

  public Optional<Long> getBuildId() {
    return buildId;
  }

  @Override
  public String toString() {
    if (buildId.isPresent()) {
      return String.format("%d-%d-%d", interProjectBuildId, mappingId, buildId.get());
    } else {
      return String.format("%d-%d-AbsentBuildId", interProjectBuildId, mappingId);
    }
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
    return Objects.equals(interProjectBuildId, m.interProjectBuildId) && Objects.equals(mappingId, m.mappingId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interProjectBuildId, mappingId);
  }
}
