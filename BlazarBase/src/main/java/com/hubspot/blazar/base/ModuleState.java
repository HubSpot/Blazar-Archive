package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class ModuleState {
  private final Module module;
  private final Optional<ModuleBuild> lastBuild;
  private final Optional<ModuleBuild> inProgressBuild;
  private final Optional<ModuleBuild> pendingBuild;

  @JsonCreator
  public ModuleState(@JsonProperty("module") Module module,
                     @JsonProperty("lastBuild") Optional<ModuleBuild> lastBuild,
                     @JsonProperty("inProgressBuild") Optional<ModuleBuild> inProgressBuild,
                     @JsonProperty("pendingBuild") Optional<ModuleBuild> pendingBuild) {
    this.module = module;

    if (lastBuild.isPresent() && !lastBuild.get().getId().isPresent()) {
      this.lastBuild = Optional.absent();
    } else {
      this.lastBuild = lastBuild;
    }

    if (inProgressBuild.isPresent() && !inProgressBuild.get().getId().isPresent()) {
      this.inProgressBuild = Optional.absent();
    } else {
      this.inProgressBuild = inProgressBuild;
    }

    if (pendingBuild.isPresent() && !pendingBuild.get().getId().isPresent()) {
      this.pendingBuild = Optional.absent();
    } else {
      this.pendingBuild = pendingBuild;
    }
  }

  public Module getModule() {
    return module;
  }

  public Optional<ModuleBuild> getLastBuild() {
    return lastBuild;
  }

  public Optional<ModuleBuild> getInProgressBuild() {
    return inProgressBuild;
  }

  public Optional<ModuleBuild> getPendingBuild() {
    return pendingBuild;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModuleState that = (ModuleState) o;
    return Objects.equals(module, that.module) &&
        Objects.equals(lastBuild, that.lastBuild) &&
        Objects.equals(inProgressBuild, that.inProgressBuild) &&
        Objects.equals(pendingBuild, that.pendingBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(module, lastBuild, inProgressBuild, pendingBuild);
  }
}
