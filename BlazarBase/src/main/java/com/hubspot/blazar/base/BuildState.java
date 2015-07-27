package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class BuildState extends BuildDefinition {
  private final Optional<Build> lastBuild;
  private final Optional<Build> inProgressBuild;

  @JsonCreator
  public BuildState(@JsonProperty("gitInfo") GitInfo gitInfo,
                    @JsonProperty("module") Module module,
                    @JsonProperty("lastBuild") Optional<Build> lastBuild,
                    @JsonProperty("inProgressBuild") Optional<Build> inProgressBuild) {
    super(gitInfo, module);

    if (lastBuild.isPresent() && !lastBuild.get().getId().isPresent()) {
      this.lastBuild = Optional.absent();
    } else {
      this.lastBuild = lastBuild;
    }

    if (inProgressBuild.isPresent() && !inProgressBuild.get().getId().isPresent()) {
      this.inProgressBuild = Optional.absent();
    } else {
      this.inProgressBuild = inProgressBuild;
    }
  }

  public Optional<Build> getLastBuild() {
    return lastBuild;
  }

  public Optional<Build> getInProgressBuild() {
    return inProgressBuild;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildState that = (BuildState) o;
    return super.equals(o) &&
        Objects.equals(lastBuild, that.lastBuild) &&
        Objects.equals(inProgressBuild, that.inProgressBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lastBuild, inProgressBuild);
  }
}
