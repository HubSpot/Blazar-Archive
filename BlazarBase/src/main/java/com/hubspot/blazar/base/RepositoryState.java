package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class RepositoryState {
  private final GitInfo gitInfo;
  private final Optional<RepositoryBuild> lastBuild;
  private final Optional<RepositoryBuild> inProgressBuild;
  private final Optional<RepositoryBuild> pendingBuild;

  @JsonCreator
  public RepositoryState(@JsonProperty("gitInfo") GitInfo gitInfo,
                         @JsonProperty("lastBuild") Optional<RepositoryBuild> lastBuild,
                         @JsonProperty("inProgressBuild") Optional<RepositoryBuild> inProgressBuild,
                         @JsonProperty("pendingBuild") Optional<RepositoryBuild> pendingBuild) {
    this.gitInfo = gitInfo;

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

    if (pendingBuild.isPresent() && !pendingBuild.get().getId().isPresent()) {
      this.pendingBuild = Optional.absent();
    } else {
      this.pendingBuild = pendingBuild;
    }
  }

  public GitInfo getGitInfo() {
    return gitInfo;
  }

  public Optional<RepositoryBuild> getLastBuild() {
    return lastBuild;
  }

  public Optional<RepositoryBuild> getInProgressBuild() {
    return inProgressBuild;
  }

  public Optional<RepositoryBuild> getPendingBuild() {
    return pendingBuild;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RepositoryState that = (RepositoryState) o;
    return Objects.equals(gitInfo, that.gitInfo) &&
        Objects.equals(lastBuild, that.lastBuild) &&
        Objects.equals(inProgressBuild, that.inProgressBuild) &&
        Objects.equals(pendingBuild, that.pendingBuild);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gitInfo, lastBuild, inProgressBuild, pendingBuild);
  }
}
