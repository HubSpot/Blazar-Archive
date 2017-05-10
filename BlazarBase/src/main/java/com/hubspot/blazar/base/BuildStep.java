package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
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
    this.name = MoreObjects.firstNonNull(name, Optional.<String>absent());
    this.description = MoreObjects.firstNonNull(description, Optional.<String>absent());
    this.commands = MoreObjects.firstNonNull(commands, Collections.<BuildCommand>emptyList());
    this.activeByDefault = MoreObjects.firstNonNull(activeByDefault, Optional.<Boolean>absent()).or(true);
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
        Optional.absent(),
        Optional.absent(),
        Collections.singletonList(BuildCommand.fromString(command)),
        Optional.absent()
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuildStep step = (BuildStep) o;
    return activeByDefault == step.activeByDefault &&
        Objects.equals(name, step.name) &&
        Objects.equals(description, step.description) &&
        Objects.equals(commands, step.commands);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, commands, activeByDefault);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("description", description)
        .add("commands", commands)
        .add("activeByDefault", activeByDefault)
        .toString();
  }
}
