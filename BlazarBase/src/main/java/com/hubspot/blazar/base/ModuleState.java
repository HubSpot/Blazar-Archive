package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class ModuleState {

  private final Module module;
  private final Optional<ModuleBuild> lastSuccessfulBuild;
  private final Optional<ModuleBuild> lastNonSkippedBuild;
  private final Optional<ModuleBuild> lastBuild;
  private final Optional<ModuleBuild> inProgressBuild;
  private final Optional<ModuleBuild> pendingBuild;

  @JsonCreator
  public ModuleState(@JsonProperty("module") Module module,
                     @JsonProperty("lastSuccessfulBuild") Optional<ModuleBuild> lastSuccessfulBuild,
                     @JsonProperty("lastNonSkippedBuild") Optional<ModuleBuild> lastNonSkippedBuild,
                     @JsonProperty("lastBuild") Optional<ModuleBuild> lastBuild,
                     @JsonProperty("inProgressBuild") Optional<ModuleBuild> inProgressBuild,
                     @JsonProperty("pendingBuild") Optional<ModuleBuild> pendingBuild) {
    this.module = module;
    this.lastSuccessfulBuild = lastSuccessfulBuild;
    this.lastNonSkippedBuild = lastNonSkippedBuild;
    this.lastBuild = lastBuild;
    this.inProgressBuild = inProgressBuild;
    this.pendingBuild = pendingBuild;
  }

  public Module getModule() {
    return module;
  }

  public Optional<ModuleBuild> getLastSuccessfulBuild() {
    return lastSuccessfulBuild;
  }

  public Optional<ModuleBuild> getLastNonSkippedBuild() {
    return lastNonSkippedBuild;
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModuleState that = (ModuleState) o;
    return Objects.equals(this.module, that.module) &&
        Objects.equals(this.lastSuccessfulBuild, that.lastSuccessfulBuild) &&
        Objects.equals(this.lastNonSkippedBuild, that.lastNonSkippedBuild) &&
        Objects.equals(this.lastBuild, that.lastNonSkippedBuild) &&
        Objects.equals(this.inProgressBuild, that.inProgressBuild) &&
        Objects.equals(this.pendingBuild, that.pendingBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(module, lastSuccessfulBuild, lastNonSkippedBuild, lastBuild, inProgressBuild, pendingBuild);
  }
}
