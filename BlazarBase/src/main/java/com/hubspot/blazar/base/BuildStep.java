package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class BuildStep {
  private final Optional<String> name;
  private final Optional<String> description;
  private final List<BuildCommand> commands;
  private final boolean activeByDefault;

  @JsonCreator
  public BuildStep(@JsonProperty("name") Optional<String> name,
                   @JsonProperty("description") Optional<String> description,
                   @JsonProperty("commands") List<BuildCommand> commands,
                   @JsonProperty("activeByDefault") Optional<Boolean> activeByDefault) {
    this.name = Objects.firstNonNull(name, Optional.<String>absent());
    this.description = Objects.firstNonNull(description, Optional.<String>absent());
    this.commands = commands;
    this.activeByDefault = Objects.firstNonNull(activeByDefault, Optional.<Boolean>absent()).or(true);
  }

  public Optional<String> getName() {
    return name;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public List<BuildCommand> getCommands() {
    return commands;
  }

  public boolean isActiveByDefault() {
    return activeByDefault;
  }

  @JsonCreator
  public static BuildStep fromString(String command) {
    return new BuildStep(
        Optional.<String>absent(),
        Optional.<String>absent(),
        Collections.singletonList(BuildCommand.fromString(command)),
        Optional.<Boolean>absent()
    );
  }
}
