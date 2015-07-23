package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BuildDefinition {
  private final GitInfo gitInfo;
  private final Module module;

  @JsonCreator
  public BuildDefinition(@JsonProperty("gitInfo") GitInfo gitInfo, @JsonProperty("module") Module module) {
    this.gitInfo = gitInfo;
    this.module = module;
  }

  public GitInfo getGitInfo() {
    return gitInfo;
  }

  public Module getModule() {
    return module;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildDefinition that = (BuildDefinition) o;
    return Objects.equals(gitInfo, that.gitInfo) && Objects.equals(module, that.module);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gitInfo, module);
  }
}
