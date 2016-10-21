package com.hubspot.blazar.data.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.branch.BranchStatus;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.dao.StateDao;

public class BranchStatusService {
  private static final Logger LOG = LoggerFactory.getLogger(BranchStatusService.class);

  private final BranchDao branchDao;
  private StateDao stateDao;
  private final RepositoryBuildDao repositoryBuildDao;

  @Inject
  public BranchStatusService(BranchDao branchDao,
                             StateDao stateDao,
                             RepositoryBuildDao repositoryBuildDao) {
    this.branchDao = branchDao;
    this.stateDao = stateDao;
    this.repositoryBuildDao = repositoryBuildDao;
  }


  public Optional<BranchStatus> getBranchStatusById(int branchId) {
    Optional<GitInfo> maybeBranch = branchDao.get(branchId);
    if (!maybeBranch.isPresent()) {
      return Optional.absent();
    }
    GitInfo branch = maybeBranch.get();
    Set<ModuleState> moduleStates = getAllModuleStatesForBranch(branchId);
    Set<GitInfo> otherBranches = branchDao.getByRepository(branch.getRepositoryId());
    otherBranches.remove(branch);
    Set<RepositoryBuild> queuedBuilds = repositoryBuildDao.getRepositoryBuildsByState(branchId, ImmutableList.of(RepositoryBuild.State.QUEUED, RepositoryBuild.State.LAUNCHING));
    List<RepositoryBuild> queuedBuildsList = new ArrayList<>(queuedBuilds);
    queuedBuildsList.sort(Comparator.comparingInt(RepositoryBuild::getBuildNumber));
    return Optional.of(new BranchStatus(queuedBuildsList, moduleStates, otherBranches, branch));
  }

  private Set<ModuleState> getAllModuleStatesForBranch(int branchId) {
    long start = System.currentTimeMillis();
    Set<ModuleState> partialStates = stateDao.getPartialModuleStatesForBranch(branchId);
    Set<ModuleState> completeStates = new HashSet<>();
    for (ModuleState partialState : partialStates) {
      // this module state is partial, only has "last", "pending", and "inProgress"
      completeStates.add(buildCompleteState(partialState));
    }
    LOG.info("Built all states for branch {} in {}", branchId, System.currentTimeMillis() - start);
    return completeStates;
  }

  private ModuleState buildCompleteState(ModuleState partialState) {
    Set<ModuleBuildInfo> remainingInfo = stateDao.getLastSuccessfulAndNonSkippedBuildInfos(partialState.getModule().getId().get());
    // remaining info contains: the most recent successful build and the most recent non-skipped build
    Optional<ModuleBuildInfo> successfulInfo = Optional.absent();
    Optional<ModuleBuildInfo> otherInfo = Optional.absent();

    for (ModuleBuildInfo info : remainingInfo) {
      if (info.getModuleBuild().getState().equals(ModuleBuild.State.SUCCEEDED)) {
        successfulInfo = Optional.of(info);
      } else {
        otherInfo = Optional.of(info);
      }
    }

    // If the most recent non-skipped build is not present that means it is the successful build
    if (!otherInfo.isPresent()) {
      otherInfo = successfulInfo;
    }

    return new ModuleState(partialState.getModule(),
        successfulInfo.isPresent() ? Optional.of(successfulInfo.get().getModuleBuild()) : Optional.absent(),
        successfulInfo.isPresent() ? Optional.of(successfulInfo.get().getBranchBuild()) : Optional.absent(),
        otherInfo.isPresent() ? Optional.of(otherInfo.get().getModuleBuild()) : Optional.absent(),
        otherInfo.isPresent() ? Optional.of(otherInfo.get().getBranchBuild()) : Optional.absent(),
        partialState.getLastModuleBuild(),
        partialState.getLastBranchBuild(),
        partialState.getInProgressModuleBuild(),
        partialState.getInProgressBranchBuild(),
        partialState.getPendingModuleBuild(),
        partialState.getPendingBranchBuild());
  }

}
