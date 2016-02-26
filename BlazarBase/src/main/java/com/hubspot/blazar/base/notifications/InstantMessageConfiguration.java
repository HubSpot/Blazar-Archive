package com.hubspot.blazar.base.notifications;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class InstantMessageConfiguration {

  private final Optional<Long> id;
  private final long branchId;
  private Optional<Long> moduleId;
  private final String channelName;
  private final boolean onFinish;
  private final boolean onFail;
  private final boolean onChange;
  private final boolean onRecover;
  private final boolean active;

  @JsonCreator
  public InstantMessageConfiguration(@JsonProperty("id") Optional<Long> id,
                                     @JsonProperty("branchId") long branchId,
                                     @JsonProperty("moduleId") Optional<Long> moduleId,
                                     @JsonProperty("channelName") String channelName,
                                     @JsonProperty("onFinish") boolean onFinish,
                                     @JsonProperty("onFail") boolean onFail,
                                     @JsonProperty("onChange") boolean onChange,
                                     @JsonProperty("onRecover") boolean onRecover,
                                     @JsonProperty("active") Optional<Boolean> active) {
    this.id = id;
    this.branchId = branchId;
    this.moduleId = moduleId;
    this.channelName = channelName;
    this.onFinish = onFinish;
    this.onFail = onFail;
    this.onChange = onChange;
    this.onRecover = onRecover;
    this.active = active.isPresent() ? active.get() : true;
  }

  public Optional<Long> getId() {
    return id;
  }

  public long getBranchId() {
    return branchId;
  }

  @JsonIgnore
  public Boolean isRepoConfig(){
    return !moduleId.isPresent();
  }

  public Optional<Long> getModuleId() {
    return moduleId;
  }

  public String getChannelName() {
    return channelName;
  }

  public boolean getOnFinish() {
    return onFinish;
  }

  public boolean getOnFail() {
    return onFail;
  }

  public boolean getOnChange() {
    return onChange;
  }

  public boolean getOnRecover() {
    return onRecover;
  }

  public boolean isActive() {
    return active;
  }
}

