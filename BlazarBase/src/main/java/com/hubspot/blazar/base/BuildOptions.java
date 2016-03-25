package com.hubspot.blazar.base;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class BuildOptions {
  private final Set<Integer> moduleIds;
  private final BuildDownstreams buildDownstreams;

  public enum BuildDownstreams {
    NONE, WITHIN_REPOSITORY;
  }

  public static BuildOptions defaultOptions() {
    return new BuildOptions(ImmutableSet.<Integer>of(), BuildDownstreams.WITHIN_REPOSITORY);
  }

  @JsonCreator
  public BuildOptions(@JsonProperty("moduleIds") Set<Integer> moduleIds,
                      @JsonProperty("buildDownstreams") BuildDownstreams buildDownstreams) {
    this.moduleIds = com.google.common.base.Objects.firstNonNull(moduleIds, ImmutableSet.<Integer>of());
    this.buildDownstreams = Preconditions.checkNotNull(buildDownstreams);
  }

  public Set<Integer> getModuleIds() {
    return moduleIds;
  }

  public BuildDownstreams getBuildDownstreams() {
    return buildDownstreams;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildOptions that = (BuildOptions) o;
    return Objects.equals(moduleIds, that.moduleIds) && Objects.equals(buildDownstreams, that.buildDownstreams);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleIds, buildDownstreams);
  }
}
