package com.hubspot.blazar.base;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class InterProjectBuildStatus {

  private final long repoBuildId;
  private final Optional<Long> interProjectBuildId;
  private Optional<InterProjectBuild.State> state;
  private Map<Long, String> rootRepoBuilds;
  private final Map<Long, String> upstreamRepoBuilds;
  private final Map<Long, String> downstreamRepoBuilds;
  private final Map<Long, String> failedRepoBuilds;
  private Set<Module> cancelledDownstreamModules;

  @JsonCreator
  public InterProjectBuildStatus(@JsonProperty("repoBuildId") long repoBuildId,
                                 @JsonProperty("interProjectBuildId") Optional<Long> interProjectBuildId,
                                 @JsonProperty("state") Optional<InterProjectBuild.State> state,
                                 @JsonProperty("rootRepoBuilds") Map<Long, String> rootRepoBuilds,
                                 @JsonProperty("upstreamRepoBuilds") Map<Long, String> upstreamRepoBuilds,
                                 @JsonProperty("downstreamRepoBuilds") Map<Long, String> downstreamRepoBuilds,
                                 @JsonProperty("failedRepoBuilds") Map<Long, String> failedRepoBuilds,
                                 @JsonProperty("cancelledDownstreamModules") Set<Module> cancelledDownstreamModules) {
    this.repoBuildId = repoBuildId;
    this.interProjectBuildId = interProjectBuildId;
    this.state = state;
    this.rootRepoBuilds = rootRepoBuilds;
    this.upstreamRepoBuilds = upstreamRepoBuilds;
    this.downstreamRepoBuilds = downstreamRepoBuilds;
    this.failedRepoBuilds = failedRepoBuilds;
    this.cancelledDownstreamModules = cancelledDownstreamModules;
  }

  public long getRepoBuildId() {
    return repoBuildId;
  }

  public Optional<Long> getInterProjectBuildId() {
    return interProjectBuildId;
  }

  public Optional<InterProjectBuild.State> getState() {
    return state;
  }

  public Map<Long, String> getRootRepoBuilds() {
    return rootRepoBuilds;
  }

  public Map<Long, String> getUpstreamRepoBuilds() {
    return upstreamRepoBuilds;
  }

  public Map<Long, String> getDownstreamRepoBuilds() {
    return downstreamRepoBuilds;
  }

  public Map<Long, String> getFailedRepoBuilds() {
    return failedRepoBuilds;
  }

  public Set<Module> getCancelledDownstreamModules() {
    return cancelledDownstreamModules;
  }
}
