package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class RepoBuild {
  private final GitInfo gitInfo;
  private final Set<Module> modules;

  @JsonCreator
  public RepoBuild(@JsonProperty("gitInfo") GitInfo gitInfo, @JsonProperty("modules") Set<Module> modules) {
    this.gitInfo = gitInfo;
    this.modules = modules;
  }

  public GitInfo getGitInfo() {
    return gitInfo;
  }

  public Set<Module> getModules() {
    return modules;
  }
}
