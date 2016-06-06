package com.hubspot.blazar.base;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InterProjectBuildDependencies {

  private final long repoBuildId;
  private final Map<Long, String> upstreamRepoBuilds;
  private final Map<Long, String> downstreamRepoBuilds;
  private Set<Module> cancelledDownstreamModules;

  @JsonCreator
  public InterProjectBuildDependencies(@JsonProperty("repoBuildId") long repoBuildId,
                                       @JsonProperty("upstreamRepoBuilds") Map<Long, String> upstreamRepoBuilds,
                                       @JsonProperty("downstreamRepoBuilds") Map<Long, String> downstreamRepoBuilds,
                                       @JsonProperty("cancelledDownstreamModules") Set<Module> cancelledDownstreamModules) {
    this.repoBuildId = repoBuildId;
    this.upstreamRepoBuilds = upstreamRepoBuilds;
    this.downstreamRepoBuilds = downstreamRepoBuilds;
    this.cancelledDownstreamModules = cancelledDownstreamModules;
  }

  public long getRepoBuildId() {
    return repoBuildId;
  }

  public Map<Long, String> getUpstreamRepoBuilds() {
    return upstreamRepoBuilds;
  }

  public Map<Long, String> getDownstreamRepoBuilds() {
    return downstreamRepoBuilds;
  }

  public Set<Module> getCancelledDownstreamModules() {
    return cancelledDownstreamModules;
  }
}
