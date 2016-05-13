package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BranchSetting {

  private final long branchId;
  private final boolean triggerInterProjectBuilds;
  private final boolean interProjectBuildOptIn;

  @JsonCreator
  public BranchSetting(@JsonProperty("branchId") long branchId,
                       @JsonProperty("triggerInterProjectBuilds") boolean triggerInterProjectBuilds,
                       @JsonProperty("interProjectBuildOptIn") boolean interProjectBuildOptIn) {
    this.branchId = branchId;
    this.triggerInterProjectBuilds = triggerInterProjectBuilds;
    this.interProjectBuildOptIn = interProjectBuildOptIn;
  }

  public static BranchSetting getWithDefaultSettings(long branchId) {
    return new BranchSetting(branchId, false, false);
  }

  public long getBranchId() {
    return branchId;
  }

  public boolean isTriggerInterProjectBuilds() {
    return triggerInterProjectBuilds;
  }

  public boolean isInterProjectBuildOptIn() {
    return interProjectBuildOptIn;
  }
}
