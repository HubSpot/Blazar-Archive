package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class Event {
  private final Optional<Integer> id;
  private final Integer moduleId;
  private final long timestamp;
  private final String username;

  @JsonCreator
  public Event(@JsonProperty("id") Optional<Integer> id,
               @JsonProperty("moduleId") Integer moduleId,
               @JsonProperty("timestamp") long timestamp,
               @JsonProperty("username") String username) {

    this.id = id;
    this.moduleId = moduleId;
    this.timestamp = timestamp;
    this.username = username;
  }

  public Optional<Integer> getId() {
    return id;
  }

  public Integer getModuleId() {
    return moduleId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getUsername() {
    return username;
  }

}
