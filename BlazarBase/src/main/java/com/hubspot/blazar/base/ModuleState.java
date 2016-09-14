package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class ModuleState {

  private final Module module;
  private final Optional<ModuleBuild> lastSuccessfulModuleBuild;
  private final Optional<RepositoryBuild> lastSuccessfulRepoBuild;
  private final Optional<ModuleBuild> lastNonSkippedModuleBuild;
  private final Optional<RepositoryBuild> lastNonSkippedRepoBuild;
  private final Optional<ModuleBuild> lastModuleBuild;
  private final Optional<RepositoryBuild> lastRepoBuild;
  private final Optional<ModuleBuild> inProgressModuleBuild;
  private final Optional<RepositoryBuild> inProgressRepoBuild;
  private final Optional<ModuleBuild> pendingModuleBuild;
  private final Optional<RepositoryBuild> pendingRepoBuild;

  @JsonCreator
  public ModuleState(@JsonProperty("module") Module module,
                     @JsonProperty("lastSuccessfulModuleBuild") Optional<ModuleBuild> lastSuccessfulModuleBuild,
                     @JsonProperty("lastSuccessfulRepoBuild") Optional<RepositoryBuild> lastSuccessfulRepoBuild,
                     @JsonProperty("lastNonSkippedModuleBuild") Optional<ModuleBuild> lastNonSkippedModuleBuild,
                     @JsonProperty("lastNonSkippedRepoBuild") Optional<RepositoryBuild> lastNonSkippedRepoBuild,
                     @JsonProperty("lastModuleBuild") Optional<ModuleBuild> lastModuleBuild,
                     @JsonProperty("lastRepoBuild") Optional<RepositoryBuild> lastRepoBuild,
                     @JsonProperty("inProgressModuleBuild") Optional<ModuleBuild> inProgressModuleBuild,
                     @JsonProperty("inProgressRepoBuild") Optional<RepositoryBuild> inProgressRepoBuild,
                     @JsonProperty("pendingModuleBuild") Optional<ModuleBuild> pendingModuleBuild,
                     @JsonProperty("pendingRepoBuild") Optional<RepositoryBuild> pendingRepoBuild) {
    this.module = module;
    this.lastSuccessfulModuleBuild = lastSuccessfulModuleBuild;
    this.lastSuccessfulRepoBuild = lastSuccessfulRepoBuild;
    this.lastNonSkippedModuleBuild = lastNonSkippedModuleBuild;
    this.lastNonSkippedRepoBuild = lastNonSkippedRepoBuild;
    this.lastModuleBuild = lastModuleBuild;
    this.lastRepoBuild = lastRepoBuild;
    this.inProgressModuleBuild = inProgressModuleBuild;
    this.inProgressRepoBuild = inProgressRepoBuild;
    this.pendingModuleBuild = pendingModuleBuild;
    this.pendingRepoBuild = pendingRepoBuild;
  }

  public Module getModule() {
    return module;
  }

  public Optional<ModuleBuild> getLastSuccessfulModuleBuild() {
    return lastSuccessfulModuleBuild;
  }

  public Optional<RepositoryBuild> getLastSuccessfulRepoBuild() {
    return lastSuccessfulRepoBuild;
  }

  public Optional<ModuleBuild> getLastNonSkippedModuleBuild() {
    return lastNonSkippedModuleBuild;
  }

  public Optional<RepositoryBuild> getLastNonSkippedRepoBuild() {
    return lastNonSkippedRepoBuild;
  }

  public Optional<ModuleBuild> getLastModuleBuild() {
    return lastModuleBuild;
  }

  public Optional<RepositoryBuild> getLastRepoBuild() {
    return lastRepoBuild;
  }

  public Optional<ModuleBuild> getInProgressModuleBuild() {
    return inProgressModuleBuild;
  }

  public Optional<RepositoryBuild> getInProgressRepoBuild() {
    return inProgressRepoBuild;
  }

  public Optional<ModuleBuild> getPendingModuleBuild() {
    return pendingModuleBuild;
  }

  public Optional<RepositoryBuild> getPendingRepoBuild() {
    return pendingRepoBuild;
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
    return Objects.equals(this.lastSuccessfulModuleBuild, that.lastSuccessfulModuleBuild) &&
        Objects.equals(this.lastSuccessfulRepoBuild, that.lastSuccessfulRepoBuild) &&
        Objects.equals(this.lastNonSkippedModuleBuild, that.lastNonSkippedModuleBuild) &&
        Objects.equals(this.lastNonSkippedRepoBuild, that.lastNonSkippedRepoBuild) &&
        Objects.equals(this.lastModuleBuild, that.lastModuleBuild) &&
        Objects.equals(this.lastRepoBuild, that.lastRepoBuild) &&
        Objects.equals(this.inProgressModuleBuild, that.inProgressModuleBuild) &&
        Objects.equals(this.inProgressRepoBuild, that.inProgressRepoBuild) &&
        Objects.equals(this.pendingModuleBuild, that.pendingModuleBuild) &&
        Objects.equals(this.pendingRepoBuild, that.pendingRepoBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(module, lastSuccessfulModuleBuild , lastSuccessfulRepoBuild , lastNonSkippedModuleBuild , lastNonSkippedRepoBuild , lastModuleBuild , lastRepoBuild , inProgressModuleBuild , inProgressRepoBuild , pendingModuleBuild , pendingRepoBuild);
  }
}
