package com.hubspot.blazar.data.service;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.dao.StateDao;

@Singleton
public class StateService {
  // Tracks how long it takes to get all module states for a branch
  private static final Logger LOG = LoggerFactory.getLogger(StateService.class);
  private final StateDao stateDao;

  @Inject
  public StateService(StateDao stateDao) {
    this.stateDao = stateDao;
  }

  public Set<RepositoryState> getAllRepositoryStates() {
    return stateDao.getAllRepositoryStates();
  }

  public Set<RepositoryState> getChangedRepositoryStates(long since) {
    return stateDao.getChangedRepositoryStates(since);
  }

  public Optional<RepositoryState> getRepositoryState(int branchId) {
    return stateDao.getRepositoryState(branchId);
  }

  /**
   * Collects the ModuleState for every module on the branch.
   *
   */
  public Set<ModuleState> getAllModuleStatesForBranch(int branchId) {
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
        partialState.getLastRepoBuild(),
        partialState.getInProgressModuleBuild(),
        partialState.getInProgressRepoBuild(),
        partialState.getPendingModuleBuild(),
        partialState.getPendingRepoBuild());
  }
}
