package com.hubspot.blazar.base;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleActivityPage {

  private final List<ModuleBuildInfo> moduleBuildInfos;
  private final int remaining;

  @JsonCreator
  public ModuleActivityPage(@JsonProperty("moduleBuildInfos") List<ModuleBuildInfo> moduleBuildInfos,
                            @JsonProperty("remaining") int remaining) {

    this.moduleBuildInfos = moduleBuildInfos;
    this.remaining = remaining;
  }

  public List<ModuleBuildInfo> getModuleBuildInfos() {
    return moduleBuildInfos;
  }

  public int getRemaining() {
    return remaining;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModuleActivityPage that = (ModuleActivityPage) o;
    return Objects.equals(moduleBuildInfos, ((ModuleActivityPage) o).moduleBuildInfos) && Objects.equals(remaining, remaining);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleBuildInfos, remaining);
  }
}
