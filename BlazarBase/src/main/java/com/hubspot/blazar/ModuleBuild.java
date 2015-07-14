package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleBuild {
  private final GitInfo gitInfo;
  private final Module module;

  @JsonCreator
  public ModuleBuild(@JsonProperty("gitInfo") GitInfo gitInfo, @JsonProperty("module") Module module) {
    this.gitInfo = gitInfo;
    this.module = module;
  }

  public GitInfo getGitInfo() {
    return gitInfo;
  }

  public Module getModule() {
    return module;
  }
}
