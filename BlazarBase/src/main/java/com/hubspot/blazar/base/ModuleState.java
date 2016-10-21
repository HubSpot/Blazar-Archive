package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class ModuleState {

  private final Module module;
  private final Optional<ModuleBuild> lastSuccessfulModuleBuild;
  private final Optional<RepositoryBuild> lastSuccessfulBranchBuild;
  private final Optional<ModuleBuild> lastNonSkippedModuleBuild;
  private final Optional<RepositoryBuild> lastNonSkippedBranchBuild;
  private final Optional<ModuleBuild> lastModuleBuild;
  private final Optional<RepositoryBuild> lastBranchBuild;
  private final Optional<ModuleBuild> inProgressModuleBuild;
  private final Optional<RepositoryBuild> inProgressBranchBuild;
  private final Optional<ModuleBuild> pendingModuleBuild;
  private final Optional<RepositoryBuild> pendingBranchBuild;

  @JsonCreator
  public ModuleState(@JsonProperty("module") Module module,
                     @JsonProperty("lastSuccessfulModuleBuild") Optional<ModuleBuild> lastSuccessfulModuleBuild,
                     @JsonProperty("lastSuccessfulBranchBuild") Optional<RepositoryBuild> lastSuccessfulBranchBuild,
                     @JsonProperty("lastNonSkippedModuleBuild") Optional<ModuleBuild> lastNonSkippedModuleBuild,
                     @JsonProperty("lastNonSkippedBranchBuild") Optional<RepositoryBuild> lastNonSkippedBranchBuild,
                     @JsonProperty("lastModuleBuild") Optional<ModuleBuild> lastModuleBuild,
                     @JsonProperty("lastBranchBuild") Optional<RepositoryBuild> lastBranchBuild,
                     @JsonProperty("inProgressModuleBuild") Optional<ModuleBuild> inProgressModuleBuild,
                     @JsonProperty("inProgressBranchBuild") Optional<RepositoryBuild> inProgressBranchBuild,
                     @JsonProperty("pendingModuleBuild") Optional<ModuleBuild> pendingModuleBuild,
                     @JsonProperty("pendingBranchBuild") Optional<RepositoryBuild> pendingBranchBuild) {
    if (module == null) {
      throw new IllegalArgumentException("Cannot construct module state with null module");
    }
    this.module = module;
    this.lastSuccessfulModuleBuild = filterModuleBuild(lastSuccessfulModuleBuild);
    this.lastSuccessfulBranchBuild = filterBranchBuild(lastSuccessfulBranchBuild);
    this.lastNonSkippedModuleBuild = filterModuleBuild(lastNonSkippedModuleBuild);
    this.lastNonSkippedBranchBuild = filterBranchBuild(lastNonSkippedBranchBuild);
    this.lastModuleBuild = filterModuleBuild(lastModuleBuild);
    this.lastBranchBuild = filterBranchBuild(lastBranchBuild);
    this.inProgressModuleBuild = filterModuleBuild(inProgressModuleBuild);
    this.inProgressBranchBuild = filterBranchBuild(inProgressBranchBuild);
    this.pendingModuleBuild = filterModuleBuild(pendingModuleBuild);
    this.pendingBranchBuild =  filterBranchBuild(pendingBranchBuild);
  }

  /* When this object is built by rosetta the missing build rows are filled with nulls,
   * which results in objects like this being passed to ModuleState's constructor:
   *
   * ModuleBuild(Optional.absent(), 0, 0, 0, null, Optional.absent()...)
   *
   * This results in a "present" ModuleBuild, but it has no actual values, the filters here
   * find such elements and replace them with just Optional.absent()
   */
  private static Optional<ModuleBuild> filterModuleBuild(Optional<ModuleBuild> moduleBuildOptional) {
    return filter(moduleBuildOptional, moduleBuild -> moduleBuild.getId().isPresent());
  }

  private static Optional<RepositoryBuild> filterBranchBuild(Optional<RepositoryBuild> repositoryBuildOptional) {
    return filter(repositoryBuildOptional, repositoryBuild -> repositoryBuild.getId().isPresent());
  }

  private static <T> Optional<T> filter(Optional<T> optional, Predicate<T> predicate) {
    return optional.isPresent() && predicate.apply(optional.get()) ? optional : Optional.absent();
  }

  public Module getModule() {
    return module;
  }

  public Optional<ModuleBuild> getLastSuccessfulModuleBuild() {
    return lastSuccessfulModuleBuild;
  }

  public Optional<RepositoryBuild> getLastSuccessfulBranchBuild() {
    return lastSuccessfulBranchBuild;
  }

  public Optional<ModuleBuild> getLastNonSkippedModuleBuild() {
    return lastNonSkippedModuleBuild;
  }

  public Optional<RepositoryBuild> getLastNonSkippedBranchBuild() {
    return lastNonSkippedBranchBuild;
  }

  public Optional<ModuleBuild> getLastModuleBuild() {
    return lastModuleBuild;
  }

  public Optional<RepositoryBuild> getLastBranchBuild() {
    return lastBranchBuild;
  }

  public Optional<ModuleBuild> getInProgressModuleBuild() {
    return inProgressModuleBuild;
  }

  public Optional<RepositoryBuild> getInProgressBranchBuild() {
    return inProgressBranchBuild;
  }

  public Optional<ModuleBuild> getPendingModuleBuild() {
    return pendingModuleBuild;
  }

  public Optional<RepositoryBuild> getPendingBranchBuild() {
    return pendingBranchBuild;
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
        Objects.equals(this.lastSuccessfulBranchBuild, that.lastSuccessfulBranchBuild) &&
        Objects.equals(this.lastNonSkippedModuleBuild, that.lastNonSkippedModuleBuild) &&
        Objects.equals(this.lastNonSkippedBranchBuild, that.lastNonSkippedBranchBuild) &&
        Objects.equals(this.lastModuleBuild, that.lastModuleBuild) &&
        Objects.equals(this.lastBranchBuild, that.lastBranchBuild) &&
        Objects.equals(this.inProgressModuleBuild, that.inProgressModuleBuild) &&
        Objects.equals(this.inProgressBranchBuild, that.inProgressBranchBuild) &&
        Objects.equals(this.pendingModuleBuild, that.pendingModuleBuild) &&
        Objects.equals(this.pendingBranchBuild, that.pendingBranchBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(module, lastSuccessfulModuleBuild , lastSuccessfulBranchBuild , lastNonSkippedModuleBuild , lastNonSkippedBranchBuild , lastModuleBuild , lastBranchBuild , inProgressModuleBuild , inProgressBranchBuild , pendingModuleBuild , pendingBranchBuild);
  }

  public Builder toBuilder () {
    return new Builder(module)
        .setLastSuccessfulBranchBuild(lastSuccessfulBranchBuild)
        .setLastSuccessfulModuleBuild(lastSuccessfulModuleBuild)
        .setLastNonSkippedBranchBuild(lastNonSkippedBranchBuild)
        .setLastNonSkippedModuleBuild(lastNonSkippedModuleBuild)
        .setLastBranchBuild(lastBranchBuild)
        .setLastModuleBuild(lastModuleBuild)
        .setInProgressBranchBuild(inProgressBranchBuild)
        .setInProgressModuleBuild(inProgressModuleBuild)
        .setPendingBranchBuild(pendingBranchBuild)
        .setPendingModuleBuild(pendingModuleBuild);
  }

  public static Builder newBuilder(Module module) {
    return new Builder(module);
  }

  public static class Builder {
    private Module module;
    private Optional<ModuleBuild> lastSuccessfulModuleBuild = Optional.absent();
    private Optional<RepositoryBuild> lastSuccessfulBranchBuild = Optional.absent();
    private Optional<ModuleBuild> lastNonSkippedModuleBuild = Optional.absent();
    private Optional<RepositoryBuild> lastNonSkippedBranchBuild = Optional.absent();
    private Optional<ModuleBuild> lastModuleBuild = Optional.absent();
    private Optional<RepositoryBuild> lastBranchBuild = Optional.absent();
    private Optional<ModuleBuild> inProgressModuleBuild = Optional.absent();
    private Optional<RepositoryBuild> inProgressBranchBuild = Optional.absent();
    private Optional<ModuleBuild> pendingModuleBuild = Optional.absent();
    private Optional<RepositoryBuild> pendingBranchBuild = Optional.absent();

    public Builder(Module module) {
      this.module = module;
    }

    public void setModule(Module module) {
      this.module = module;
    }

    public Builder setLastSuccessfulModuleBuild(Optional<ModuleBuild> lastSuccessfulModuleBuild) {
      this.lastSuccessfulModuleBuild = lastSuccessfulModuleBuild;
      return this;
    }

    public Builder setLastSuccessfulBranchBuild(Optional<RepositoryBuild> lastSuccessfulBranchBuild) {
      this.lastSuccessfulBranchBuild = lastSuccessfulBranchBuild;
      return this;
    }

    public Builder setLastNonSkippedModuleBuild(Optional<ModuleBuild> lastNonSkippedModuleBuild) {
      this.lastNonSkippedModuleBuild = lastNonSkippedModuleBuild;
      return this;
    }

    public Builder setLastNonSkippedBranchBuild(Optional<RepositoryBuild> lastNonSkippedBranchBuild) {
      this.lastNonSkippedBranchBuild = lastNonSkippedBranchBuild;
      return this;
    }

    public Builder setLastModuleBuild(Optional<ModuleBuild> lastModuleBuild) {
      this.lastModuleBuild = lastModuleBuild;
      return this;
    }

    public Builder setLastBranchBuild(Optional<RepositoryBuild> lastBranchBuild) {
      this.lastBranchBuild = lastBranchBuild;
      return this;
    }

    public Builder setInProgressModuleBuild(Optional<ModuleBuild> inProgressModuleBuild) {
      this.inProgressModuleBuild = inProgressModuleBuild;
      return this;
    }

    public Builder setInProgressBranchBuild(Optional<RepositoryBuild> inProgressBranchBuild) {
      this.inProgressBranchBuild = inProgressBranchBuild;
      return this;
    }

    public Builder setPendingModuleBuild(Optional<ModuleBuild> pendingModuleBuild) {
      this.pendingModuleBuild = pendingModuleBuild;
      return this;
    }

    public Builder setPendingBranchBuild(Optional<RepositoryBuild> pendingBranchBuild) {
      this.pendingBranchBuild = pendingBranchBuild;
      return this;
    }

    public ModuleState build() {
      return new ModuleState(module, lastSuccessfulModuleBuild, lastSuccessfulBranchBuild, lastNonSkippedModuleBuild, lastNonSkippedBranchBuild, lastModuleBuild, lastBranchBuild,
           inProgressModuleBuild, inProgressBranchBuild, pendingModuleBuild,pendingBranchBuild);
    }
  }

}
