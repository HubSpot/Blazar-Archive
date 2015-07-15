package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleBuildWithState {
  private final GitInfo gitInfo;
  private final Module module;
  private final BuildState buildState;

  @JsonCreator
  public ModuleBuildWithState(@JsonProperty("gitInfo") GitInfo gitInfo,
                              @JsonProperty("module") Module module,
                              @JsonProperty("buildState") BuildState buildState) {
    this.gitInfo = gitInfo;
    this.module = module;
    this.buildState = buildState;
  }

  public GitInfo getGitInfo() {
    return gitInfo;
  }

  public Module getModule() {
    return module;
  }

  public BuildState getBuildState() {
    return buildState;
  }
}
