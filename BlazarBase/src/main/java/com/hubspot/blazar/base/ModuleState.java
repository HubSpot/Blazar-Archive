package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class ModuleState {

  private final Module module;
  private final Optional<BuildInfo> lastSuccessfulBuild;
  private final Optional<BuildInfo> lastNonSkippedBuild;
  private final Optional<BuildInfo> lastBuild;
  private final Optional<BuildInfo> inProgressBuild;
  private final Optional<BuildInfo> pendingBuild;

  @JsonCreator
  public ModuleState(@JsonProperty("module") Module module,
                     @JsonProperty("lastSuccessfulBuild") Optional<BuildInfo> lastSuccessfulBuild,
                     @JsonProperty("lastNonSkippedBuild") Optional<BuildInfo> lastNonSkippedBuild,
                     @JsonProperty("lastBuild") Optional<BuildInfo> lastBuild,
                     @JsonProperty("inProgressBuild") Optional<BuildInfo> inProgressBuild,
                     @JsonProperty("pendingBuild") Optional<BuildInfo> pendingBuild) {
    this.module = module;
    this.lastSuccessfulBuild = lastSuccessfulBuild;
    this.lastNonSkippedBuild = lastNonSkippedBuild;
    this.lastBuild = lastBuild;
    this.inProgressBuild = inProgressBuild;
    this.pendingBuild = pendingBuild;
  }


  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Optional<Module> module;
    private Optional<BuildInfo> lastSuccessfulBuild;
    private Optional<BuildInfo> lastNonSkippedBuild;
    private Optional<BuildInfo> lastBuild;
    private Optional<BuildInfo> inProgressBuild;
    private Optional<BuildInfo> pendingBuild;

    public Builder() {
      this.module = Optional.absent();
      this.lastSuccessfulBuild = Optional.absent();
      this.lastNonSkippedBuild = Optional.absent();
      this.lastBuild = Optional.absent();
      this.inProgressBuild = Optional.absent();
      this.pendingBuild = Optional.absent();
    }

    public Builder setModule(Module module) {
      this.module = Optional.of(module);
      return this;
    }

    public Builder setLastSuccessfulBuild(Optional<BuildInfo> build) {
      this.lastSuccessfulBuild = build;
      return this;
    }

    public Builder setLastNonSkippedBuild(Optional<BuildInfo> build) {
      this.lastNonSkippedBuild = build;
      return this;
    }

    public Builder setLastBuild(Optional<BuildInfo> build) {
      this.lastBuild = build;
      return this;
    }

    public Builder setInProgressBuild(Optional<BuildInfo> build) {
      this.inProgressBuild = build;
      return this;
    }

    public Builder setPendingBuild(Optional<BuildInfo> build) {
      this.pendingBuild = build;
      return this;
    }

    public ModuleState build() {
      if (!module.isPresent()) {
        throw new RuntimeException("A ModuleState must have a module, no module given to this builder");
      }
      return new ModuleState(module.get(), lastSuccessfulBuild, lastNonSkippedBuild, lastBuild, inProgressBuild, pendingBuild);
    }
  }


  public Module getModule() {
    return module;
  }

  public Optional<BuildInfo> getLastSuccessfulBuild() {
    return lastSuccessfulBuild;
  }

  public Optional<BuildInfo> getLastNonSkippedBuild() {
    return lastNonSkippedBuild;
  }

  public Optional<BuildInfo> getLastBuild() {
    return lastBuild;
  }

  public Optional<BuildInfo> getInProgressBuild() {
    return inProgressBuild;
  }

  public Optional<BuildInfo> getPendingBuild() {
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
