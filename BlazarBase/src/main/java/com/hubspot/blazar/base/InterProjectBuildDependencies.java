package com.hubspot.blazar.base;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InterProjectBuildDependencies {

  private final long repoBuildId;
  private final Set<Long> upstreams;
  private final Set<Long> downstreams;
  private Set<Module> cancelledDownstreamModules;

  @JsonCreator
  public InterProjectBuildDependencies(@JsonProperty("repoBuildId") long repoBuildId,
                                       @JsonProperty("upstreams") Set<Long> upstreams,
                                       @JsonProperty("downstreams") Set<Long> downstreams,
                                       @JsonProperty("cancelledDownstreamModules") Set<Module> cancelledDownstreamModules) {
    this.repoBuildId = repoBuildId;
    this.upstreams = upstreams;
    this.downstreams = downstreams;
    this.cancelledDownstreamModules = cancelledDownstreamModules;
  }

  public long getRepoBuildId() {
    return repoBuildId;
  }

  public Set<Long> getUpstreams() {
    return upstreams;
  }

  public Set<Long> getDownstreams() {
    return downstreams;
  }

  public Set<Module> getCancelledDownstreamModules() {
    return cancelledDownstreamModules;
  }
}
