package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;

public class BuildStep {
  private final Optional<String> description;
  private final List<String> commands;

  @JsonCreator
  public BuildStep(@JsonProperty("description") Optional<String> description,
                   @JsonProperty("commands") List<String> commands) {
    this.description = description;
    this.commands = commands;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public List<String> getCommands() {
    return commands;
  }

  @JsonCreator
  public static BuildStep fromString(String command) {
    return new BuildStep(Optional.<String>absent(), Collections.singletonList(command));
  }
}
