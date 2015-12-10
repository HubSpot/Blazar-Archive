package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class ModuleState {
  private final Module module;
  private final Optional<RepositoryBuild> lastBuild;
  private final Optional<RepositoryBuild> inProgressBuild;
  private final Optional<RepositoryBuild> pendingBuild;

  @JsonCreator
  public ModuleState(@JsonProperty("module") Module module,
                     @JsonProperty("lastBuild") Optional<RepositoryBuild> lastBuild,
                     @JsonProperty("inProgressBuild") Optional<RepositoryBuild> inProgressBuild,
                     @JsonProperty("pendingBuild") Optional<RepositoryBuild> pendingBuild) {
    this.module = module;
    this.lastBuild = lastBuild;
    this.inProgressBuild = inProgressBuild;
    this.pendingBuild = pendingBuild;
  }

  public Module getModule() {
    return module;
  }

  public Optional<RepositoryBuild> getLastBuild() {
    return lastBuild;
  }

  public Optional<RepositoryBuild> getInProgressBuild() {
    return inProgressBuild;
  }

  public Optional<RepositoryBuild> getPendingBuild() {
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
