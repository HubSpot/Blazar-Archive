package com.hubspot.blazar.base;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class InterProjectBuildStatus {

  private final long repoBuildId;
  private Optional<InterProjectBuild.State> state;
  private final Map<Long, String> upstreamRepoBuilds;
  private final Map<Long, String> downstreamRepoBuilds;
  private Set<Module> cancelledDownstreamModules;

  @JsonCreator
  public InterProjectBuildStatus(@JsonProperty("repoBuildId") long repoBuildId,
                                 @JsonProperty("state") Optional<InterProjectBuild.State> state,
                                 @JsonProperty("upstreamRepoBuilds") Map<Long, String> upstreamRepoBuilds,
                                 @JsonProperty("downstreamRepoBuilds") Map<Long, String> downstreamRepoBuilds,
                                 @JsonProperty("cancelledDownstreamModules") Set<Module> cancelledDownstreamModules) {
    this.repoBuildId = repoBuildId;
    this.state = state;
    this.upstreamRepoBuilds = upstreamRepoBuilds;
    this.downstreamRepoBuilds = downstreamRepoBuilds;
    this.cancelledDownstreamModules = cancelledDownstreamModules;
  }

  public long getRepoBuildId() {
    return repoBuildId;
  }

  public Optional<InterProjectBuild.State> getState() {
    return state;
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
