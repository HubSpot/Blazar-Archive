package com.hubspot.blazar.base;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class BuildOptions {

  private final Set<Integer> moduleIds;
  private final boolean buildDownstreamModules;

  @JsonCreator
  public BuildOptions(@JsonProperty("moduleIds") Set<Integer> moduleIds, @JsonProperty("buildDownstreamModules") boolean buildDownstreamModules) {
    this.moduleIds = Objects.firstNonNull(moduleIds, ImmutableSet.<Integer>of());
    this.buildDownstreamModules = buildDownstreamModules;
  }

  public Set<Integer> getModuleIds() {
    return moduleIds;
  }

  public boolean isBuildDownstreamModules() {
    return buildDownstreamModules;
  }

}
