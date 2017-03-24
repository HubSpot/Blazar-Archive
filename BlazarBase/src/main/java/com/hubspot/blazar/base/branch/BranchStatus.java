package com.hubspot.blazar.base.branch;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;

public class BranchStatus {
  private final List<RepositoryBuild> queuedBuilds;
  private final Optional<RepositoryBuild> activeBuild;
  private final Set<ModuleState> moduleStates;
  private final Set<GitInfo> otherBranches;
  private final Set<MalformedFile> malformedFiles;
  private final GitInfo branch;

  @JsonCreator
  public BranchStatus(@JsonProperty("queuedBuilds") List<RepositoryBuild> queuedBuilds,
                      @JsonProperty("activeBuild") Optional<RepositoryBuild> activeBuild,
                      @JsonProperty("moduleStates") Set<ModuleState> moduleStates,
                      @JsonProperty("otherBranches") Set<GitInfo> otherBranches,
                      @JsonProperty("malformedFiles") Set<MalformedFile> malformedFiles,
                      @JsonProperty("branch") GitInfo branch) {
    this.queuedBuilds = queuedBuilds;
    this.activeBuild = activeBuild;
    this.moduleStates = moduleStates;
    this.otherBranches = otherBranches;
    this.malformedFiles = malformedFiles;
    this.branch = branch;
  }

  public List<RepositoryBuild> getQueuedBuilds() {
    return queuedBuilds;
  }

  public Optional<RepositoryBuild> getActiveBuild() {
    return activeBuild;
  }

  public Set<ModuleState> getModuleStates() {
    return moduleStates;
  }

  public Set<GitInfo> getOtherBranches() {
    return otherBranches;
  }

  public Set<MalformedFile> getMalformedFiles() {
    return malformedFiles;
  }

  public GitInfo getBranch() {
    return branch;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BranchStatus that = (BranchStatus) o;
    return Objects.equal(queuedBuilds, that.queuedBuilds) &&
        Objects.equal(activeBuild, that.activeBuild) &&
        Objects.equal(moduleStates, that.moduleStates) &&
        Objects.equal(otherBranches, that.otherBranches) &&
        Objects.equal(malformedFiles, that.malformedFiles) &&
        Objects.equal(branch, that.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(queuedBuilds, activeBuild, moduleStates, otherBranches, malformedFiles, branch);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("queuedBuilds", queuedBuilds)
        .add("activeBuild", activeBuild)
        .add("moduleStates", moduleStates)
        .add("otherBranches", otherBranches)
        .add("malformedFiles", malformedFiles)
        .add("branch", branch)
        .toString();
  }

  public class Builder {
    private List<RepositoryBuild> queuedBuilds = ImmutableList.of();
    private Optional<RepositoryBuild> activeBuild = Optional.absent();
    private Set<ModuleState> moduleStates = ImmutableSet.of();
    private Set<GitInfo> otherBranches = ImmutableSet.of();
    private Set<MalformedFile> malformedFiles = ImmutableSet.of();
    private GitInfo branch;


    public Builder(GitInfo branch) {
      this.branch = branch;
    }

    public Builder setQueuedBuilds(List<RepositoryBuild> queuedBuilds) {
      this.queuedBuilds = queuedBuilds;
      return this;
    }

    public Builder setActiveBuild(Optional<RepositoryBuild> activeBuild) {
      this.activeBuild = activeBuild;
      return this;
    }

    public Builder setModuleStates(Set<ModuleState> moduleStates) {
      this.moduleStates = moduleStates;
      return this;
    }

    public Builder setOtherBranches(Set<GitInfo> otherBranches) {
      this.otherBranches = otherBranches;
      return this;
    }

    public Builder setMalformedFiles(Set<MalformedFile> malformedFiles) {
      this.malformedFiles = malformedFiles;
      return this;
    }

    public Builder setBranch(GitInfo branch) {
      this.branch = branch;
      return this;
    }

    public BranchStatus build() {
      return new BranchStatus(queuedBuilds, activeBuild, moduleStates, otherBranches, malformedFiles, branch);
    }
  }
}
