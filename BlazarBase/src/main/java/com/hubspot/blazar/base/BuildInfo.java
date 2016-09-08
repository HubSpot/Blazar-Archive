package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildInfo {

  private final RepositoryBuild repositoryBuild;
  private final ModuleBuild moduleBuild;

  @JsonCreator
  public BuildInfo(@JsonProperty("repositoryBuild") RepositoryBuild repositoryBuild,
                   @JsonProperty("moduleBuild") ModuleBuild moduleBuild) {
    this.repositoryBuild = repositoryBuild;
    this.moduleBuild = moduleBuild;
  }

  public RepositoryBuild getRepositoryBuild() {
    return repositoryBuild;
  }

  public ModuleBuild getModuleBuild() {
    return moduleBuild;
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
        .add("branchId", repositoryBuild.getBranchId())
        .add("repositoryBuildId", repositoryBuild.getId())
        .add("moduleBuildId", moduleBuild.getId())
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildInfo buildInfo = (BuildInfo) o;
    return Objects.equals(repositoryBuild, buildInfo.repositoryBuild) && Objects.equals(moduleBuild, buildInfo.moduleBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repositoryBuild, moduleBuild);
  }
}
