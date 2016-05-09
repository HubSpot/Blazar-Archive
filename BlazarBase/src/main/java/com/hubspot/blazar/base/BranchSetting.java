package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BranchSetting {

  private final long branchId;
  private final boolean triggerInterProjectBuilds;

  @JsonCreator
  public BranchSetting(@JsonProperty("branchId") long branchId,
                       @JsonProperty("triggerInterProjectBuilds") boolean triggerInterProjectBuilds) {
    this.branchId = branchId;
    this.triggerInterProjectBuilds = triggerInterProjectBuilds;
  }

  public static BranchSetting getWithDefaultSettings(long branchId) {
    return new BranchSetting(branchId, false);
  }

  public long getBranchId() {
    return branchId;
  }

  public boolean isTriggerInterProjectBuilds() {
    return triggerInterProjectBuilds;
  }
}
