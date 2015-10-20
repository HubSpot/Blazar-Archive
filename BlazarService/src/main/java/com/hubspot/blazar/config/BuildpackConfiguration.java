package com.hubspot.blazar.config;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubspot.blazar.base.GitInfo;
import com.google.common.base.Optional;

public class BuildpackConfiguration {
  @JsonProperty
  @NotNull
  private Optional<GitInfo> defaultBuildpack = Optional.absent();

  @JsonProperty
  @NotNull
  private Optional<GitInfo> deployableBuildpack = Optional.absent();

  @JsonProperty
  @NotNull
  private Map<String, GitInfo> branchBuildpack = new HashMap<>();

  @JsonProperty
  @NotNull
  private Map<GitInfo, GitInfo> repoBuildpack = new HashMap<>();

  public Optional<GitInfo> getDefaultBuildpack() {
    return defaultBuildpack;
  }

  public void setDefaultBuildpack(Optional<GitInfo> defaultBuildpack) {
    this.defaultBuildpack = defaultBuildpack;
  }

  public Optional<GitInfo> getDeployableBuildpack() {
    return deployableBuildpack;
  }

  public void setDeployableBuildpack(Optional<GitInfo> deployableBuildpack) {
    this.deployableBuildpack = deployableBuildpack;
  }

  public Map<String, GitInfo> getBranchBuildpack() {
    return branchBuildpack;
  }

  public void setBranchBuildpack(Map<String, GitInfo> branchBuildpack) {
    this.branchBuildpack = branchBuildpack;
  }

  public Map<GitInfo, GitInfo> getRepoBuildpack() {
    return repoBuildpack;
  }

  public void setRepoBuildpack(Map<GitInfo, GitInfo> repoBuildpack) {
    this.repoBuildpack = repoBuildpack;
  }
}
