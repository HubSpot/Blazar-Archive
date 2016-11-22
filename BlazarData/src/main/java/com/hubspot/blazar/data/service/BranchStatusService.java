package com.hubspot.blazar.data.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.branch.BranchStatus;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.MalformedFileDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.dao.StateDao;

public class BranchStatusService {
  private static final Logger LOG = LoggerFactory.getLogger(BranchStatusService.class);

  private final BranchDao branchDao;
  private StateDao stateDao;
  private MalformedFileDao malformedFileDao;
  private final RepositoryBuildDao repositoryBuildDao;

  @Inject
  public BranchStatusService(BranchDao branchDao,
                             StateDao stateDao,
                             MalformedFileDao malformedFileDao,
                             RepositoryBuildDao repositoryBuildDao) {
    this.branchDao = branchDao;
    this.stateDao = stateDao;
    this.malformedFileDao = malformedFileDao;
    this.repositoryBuildDao = repositoryBuildDao;
  }


  public Optional<BranchStatus> getBranchStatusById(int branchId) {
    Optional<GitInfo> maybeBranch = branchDao.get(branchId);
    if (!maybeBranch.isPresent()) {
      return Optional.absent();
    }
    GitInfo branch = maybeBranch.get();
    Set<ModuleState> moduleStates = getModuleStatesForBranch(branchId);
    Set<GitInfo> otherBranches = branchDao.getByRepository(branch.getRepositoryId()).stream().filter(GitInfo::isActive).collect(Collectors.toSet());
    otherBranches.remove(branch);
    Set<RepositoryBuild> queuedBuilds = repositoryBuildDao.getRepositoryBuildsByState(branchId, ImmutableList.of(RepositoryBuild.State.QUEUED));
    List<RepositoryBuild> queuedBuildsList = new ArrayList<>(queuedBuilds);
    queuedBuildsList.sort(Comparator.comparingInt(RepositoryBuild::getBuildNumber));
    Set<MalformedFile> malformedFiles = malformedFileDao.getMalformedFiles(branchId);

    // We can only have up to 1 build in either of these states at a time
    Set<RepositoryBuild> launchingOrInProgressBuilds = repositoryBuildDao
        .getRepositoryBuildsByState(branchId, ImmutableList.of(RepositoryBuild.State.LAUNCHING, RepositoryBuild.State.IN_PROGRESS));

    Optional<RepositoryBuild> maybeActiveBuild = launchingOrInProgressBuilds.isEmpty() ? Optional.absent() : Optional.of(launchingOrInProgressBuilds.iterator().next());
    return Optional.of(new BranchStatus(queuedBuildsList, maybeActiveBuild, moduleStates, otherBranches, malformedFiles, branch));
  }

  private Set<ModuleState> getModuleStatesForBranch(int branchId) {
    long start = System.currentTimeMillis();
    // We retrieve the required info per state in two steps
    // In the first step we retrieve the module itself along with the build info about the 'last', 'in progress'
    // and 'pending' builds for the given branch as well as the included modules
    Set<ModuleState> partialStates = stateDao.getLastAndInProgressAndPendingBuildsForBranchAndIncludedModules(branchId);
    Set<ModuleState> completeStates = new HashSet<>();
    // In the second state we enrich the module state with info about the 'last successful' and 'non-skipped' module builds
    for (ModuleState partialState : partialStates) {
      completeStates.add(completePartialModuleStateWithLastSuccessfulAndNonSkippedModuleBuilds(partialState));
    }
    LOG.info("Built all states for branch {} in {}", branchId, System.currentTimeMillis() - start);
    return completeStates;
  }

  // remaining info contains: the most recent successful build and the most recent non-skipped build
  private ModuleState completePartialModuleStateWithLastSuccessfulAndNonSkippedModuleBuilds(ModuleState partialState) {
    Set<ModuleBuildInfo> lastSuccessfulAndNonSkippedModuleBuilds =
        stateDao.getLastSuccessfulAndNonSkippedModuleBuilds(partialState.getModule().getId().get());

    Optional<ModuleBuildInfo> successfulModuleBuildInfo = Optional.absent();
    Optional<ModuleBuildInfo> nonSkippedModuleBuildInfo = Optional.absent();

    for (ModuleBuildInfo moduleBuildInfo : lastSuccessfulAndNonSkippedModuleBuilds) {
      if (moduleBuildInfo.getModuleBuild().getState().equals(ModuleBuild.State.SUCCEEDED)) {
        successfulModuleBuildInfo = Optional.of(moduleBuildInfo);
      } else {
        nonSkippedModuleBuildInfo = Optional.of(moduleBuildInfo);
      }
    }

    // If the most recent non-skipped build is not present that means it is the successful build
    if (!nonSkippedModuleBuildInfo.isPresent()) {
      nonSkippedModuleBuildInfo = successfulModuleBuildInfo;
    }

    return new ModuleState(partialState.getModule(),
        successfulModuleBuildInfo.isPresent() ? Optional.of(successfulModuleBuildInfo.get().getModuleBuild()) : Optional.absent(),
        successfulModuleBuildInfo.isPresent() ? Optional.of(successfulModuleBuildInfo.get().getBranchBuild()) : Optional.absent(),
        nonSkippedModuleBuildInfo.isPresent() ? Optional.of(nonSkippedModuleBuildInfo.get().getModuleBuild()) : Optional.absent(),
        nonSkippedModuleBuildInfo.isPresent() ? Optional.of(nonSkippedModuleBuildInfo.get().getBranchBuild()) : Optional.absent(),
        partialState.getLastModuleBuild(),
        partialState.getLastBranchBuild(),
        partialState.getInProgressModuleBuild(),
        partialState.getInProgressBranchBuild(),
        partialState.getPendingModuleBuild(),
        partialState.getPendingBranchBuild());
  }

}
