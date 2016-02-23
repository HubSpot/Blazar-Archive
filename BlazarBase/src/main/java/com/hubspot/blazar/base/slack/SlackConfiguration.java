package com.hubspot.blazar.base.slack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class SlackConfiguration {

  private final Optional<Long> id;
  private final long repositoryId;
  private Optional<Long> moduleId;
  private final String channelName;
  private final boolean onFinish;
  private final boolean onFail;
  private final boolean onChange;
  private final boolean onRecover;
  private final boolean active;

  public SlackConfiguration(@JsonProperty("id") Optional<Long> id,
                            @JsonProperty("repositoryId") long repositoryId,
                            @JsonProperty("moduleId") Optional<Long> moduleId,
                            @JsonProperty("channelName") String channelName,
                            @JsonProperty("onFinish") boolean onFinish,
                            @JsonProperty("onFail") boolean onFail,
                            @JsonProperty("onChange") boolean onChange,
                            @JsonProperty("onRecover") boolean onRecover,
                            @JsonProperty("active") boolean active) {
    this.id = id;
    this.repositoryId = repositoryId;
    this.moduleId = moduleId;
    this.channelName = channelName;
    this.onFinish = onFinish;
    this.onFail = onFail;
    this.onChange = onChange;
    this.onRecover = onRecover;
    this.active = active;
  }

  public Optional<Long> getId() {
    return id;
  }

  public long getRepositoryId() {
    return repositoryId;
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

  public boolean isOnFinish() {
    return onFinish;
  }

  public boolean isOnFail() {
    return onFail;
  }

  public boolean isOnChange() {
    return onChange;
  }

  public boolean isOnRecover() {
    return onRecover;
  }

  public boolean isActive() {
    return active;
  }
}

