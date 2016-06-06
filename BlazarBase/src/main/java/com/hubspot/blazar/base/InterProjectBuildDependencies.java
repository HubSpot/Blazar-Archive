package com.hubspot.blazar.base;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InterProjectBuildDependencies {

  private final long repoBuildId;
  private final Set<Long> upstreamRepoBuilds;
  private final Set<Long> downstreamRepoBuilds;
  private Set<Module> cancelledDownstreamModules;

  @JsonCreator
  public InterProjectBuildDependencies(@JsonProperty("repoBuildId") long repoBuildId,
                                       @JsonProperty("upstreamRepoBuilds") Set<Long> upstreamRepoBuilds,
                                       @JsonProperty("downstreamRepoBuilds") Set<Long> downstreamRepoBuilds,
                                       @JsonProperty("cancelledDownstreamModules") Set<Module> cancelledDownstreamModules) {
    this.repoBuildId = repoBuildId;
    this.upstreamRepoBuilds = upstreamRepoBuilds;
    this.downstreamRepoBuilds = downstreamRepoBuilds;
    this.cancelledDownstreamModules = cancelledDownstreamModules;
  }

  public long getRepoBuildId() {
    return repoBuildId;
  }

  public Set<Long> getUpstreamRepoBuilds() {
    return upstreamRepoBuilds;
  }

  public Set<Long> getDownstreamRepoBuilds() {
    return downstreamRepoBuilds;
  }

  public Set<Module> getCancelledDownstreamModules() {
    return cancelledDownstreamModules;
  }
}
