package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class BuildStep {
  private final Optional<String> description;
  private final List<BuildCommand> commands;

  @JsonCreator
  public BuildStep(@JsonProperty("description") Optional<String> description,
                   @JsonProperty("commands") List<BuildCommand> commands) {
    this.description = description;
    this.commands = commands;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public List<BuildCommand> getCommands() {
    return commands;
  }

  @JsonCreator
  public static BuildStep fromString(String command) {
    return new BuildStep(Optional.<String>absent(), Collections.<BuildCommand>singletonList(BuildCommand.buildCommandFromString(command)));
  }
}
