package com.hubspot.blazar.base.branch;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;

public class BranchStatus {
  private final List<RepositoryBuild> queuedBuilds;
  private final Set<ModuleState> moduleStates;
  private final Set<GitInfo> otherBranches;
  private Set<MalformedFile> malformedFiles;
  private GitInfo branch;

  @JsonCreator
  public BranchStatus(@JsonProperty("queuedBuilds") List<RepositoryBuild> queuedBuilds,
                      @JsonProperty("moduleStates") Set<ModuleState> moduleStates,
                      @JsonProperty("otherBranches") Set<GitInfo> otherBranches,
                      @JsonProperty("malformedFiles") Set<MalformedFile> malformedFiles,
                      @JsonProperty("branch") GitInfo branch) {
    this.queuedBuilds = queuedBuilds;
    this.moduleStates = moduleStates;
    this.otherBranches = otherBranches;
    this.malformedFiles = malformedFiles;
    this.branch = branch;
  }

  public List<RepositoryBuild> getQueuedBuilds() {
    return queuedBuilds;
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
    return Objects.equals(queuedBuilds, that.queuedBuilds) &&
        Objects.equals(moduleStates, that.moduleStates) &&
        Objects.equals(otherBranches, that.otherBranches) &&
        Objects.equals(malformedFiles, that.malformedFiles) &&
        Objects.equals(branch, that.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(queuedBuilds, moduleStates, otherBranches, malformedFiles, branch);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("queuedBuilds", queuedBuilds)
        .add("moduleStates", moduleStates)
        .add("otherBranches", otherBranches)
        .add("malformedFiles", malformedFiles)
        .add("branch", branch)
        .toString();
  }
}
