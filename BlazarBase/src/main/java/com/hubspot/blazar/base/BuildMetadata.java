package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class BuildMetadata {
  public enum TriggeringEvent {
    PUSH, MANUAL, INTER_PROJECT
  }

  private final TriggeringEvent triggeringEvent;
  private Optional<String> user;

  @JsonCreator
  public BuildMetadata(@JsonProperty("type") TriggeringEvent triggeringEvent,
                       @JsonProperty("user") Optional<String> user) {
    this.triggeringEvent = triggeringEvent;
    this.user = user;
  }

  public static BuildMetadata push(String login) {
    return new BuildMetadata(TriggeringEvent.PUSH, Optional.of(login));
  }

  public static BuildMetadata manual(Optional<String> user) {
    return new BuildMetadata(TriggeringEvent.MANUAL, user);
  }

  public static BuildMetadata interProjectBuild() {
    return new BuildMetadata(TriggeringEvent.INTER_PROJECT, Optional.absent());
  }

  // For backwards compatibility
  @JsonProperty("type")
  public TriggeringEvent getTriggeringEvent() {
    return triggeringEvent;
  }

  public Optional<String> getUser() {
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildMetadata that = (BuildMetadata) o;
    return Objects.equals(triggeringEvent, that.triggeringEvent) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(triggeringEvent, user);
  }
}
