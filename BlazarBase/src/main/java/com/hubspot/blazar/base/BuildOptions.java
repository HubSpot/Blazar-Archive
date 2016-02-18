package com.hubspot.blazar.base;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
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
  public BuildOptions(@JsonProperty("moduleIds") Set<Integer> moduleIds, @JsonProperty("buildDownstreams") BuildDownstreams buildDownstreams) {
    this.moduleIds = Objects.firstNonNull(moduleIds, ImmutableSet.<Integer>of());
    this.buildDownstreams = buildDownstreams;
  }

  public Set<Integer> getModuleIds() {
    return moduleIds;
  }

  public BuildDownstreams getBuildDownstreams() {
    return buildDownstreams;
  }

}
