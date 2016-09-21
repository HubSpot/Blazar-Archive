package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleBuildInfo {

  private final ModuleBuild moduleBuild;
  private final RepositoryBuild branchBuild;

  @JsonCreator
  public ModuleBuildInfo (@JsonProperty("moduleBuild") ModuleBuild moduleBuild,
                          @JsonProperty("branchBuild") RepositoryBuild branchBuild) {

    this.moduleBuild = moduleBuild;
    this.branchBuild = branchBuild;
  }

  public ModuleBuild getModuleBuild() {
    return moduleBuild;
  }

  public RepositoryBuild getBranchBuild() {
    return branchBuild;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModuleBuildInfo that = (ModuleBuildInfo) o;

    return Objects.equals(moduleBuild, that.moduleBuild) && Objects.equals(branchBuild, that.branchBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleBuild, branchBuild);
  }
}
