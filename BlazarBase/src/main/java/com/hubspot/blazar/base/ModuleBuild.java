package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ModuleBuild extends BuildDefinition {
  private final Build build;

  @JsonCreator
  public ModuleBuild(@JsonProperty("gitInfo") GitInfo gitInfo,
                     @JsonProperty("module") Module module,
                     @JsonProperty("build") Build build) {
    super(gitInfo, module);
    this.build = build;
  }

  public Build getBuild() {
    return build;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModuleBuild that = (ModuleBuild) o;
    return super.equals(o) && Objects.equals(build, that.build);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), build);
  }
}
