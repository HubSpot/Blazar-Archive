package com.hubspot.blazar.util;

import static com.hubspot.blazar.base.BuildTrigger.Type.INTER_PROJECT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.discovery.ModuleDiscoveryHandler;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos.Commit;

@Singleton
public class RepositoryBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryBuildLauncher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final BranchService branchService;
  private final ModuleService moduleService;
  private final DependenciesService dependenciesService;
  private final ModuleDiscoveryService moduleDiscoveryService;
  private final ModuleDiscoveryHandler moduleDiscoveryHandler;
  private final GitHubHelper gitHubHelper;

  @Inject
  public RepositoryBuildLauncher(RepositoryBuildService repositoryBuildService,
                                 BranchService branchService,
                                 ModuleService moduleService,
                                 DependenciesService dependenciesService,
                                 ModuleDiscoveryService moduleDiscoveryService,
                                 ModuleDiscoveryHandler moduleDiscoveryHandler,
                                 GitHubHelper gitHubHelper) {
    this.repositoryBuildService = repositoryBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.dependenciesService = dependenciesService;
    this.moduleDiscoveryService = moduleDiscoveryService;
    this.moduleDiscoveryHandler = moduleDiscoveryHandler;
    this.gitHubHelper = gitHubHelper;
  }

  public void launch(RepositoryBuild queued, Optional<RepositoryBuild> previous) throws Exception {
    GitInfo branch = branchService.get(queued.getBranchId()).get();
    CommitInfo commitInfo = calculateCommitInfoForBuild(branch, queued, previous);

    moduleDiscoveryHandler.updateModules(branch, commitInfo, true);
    // We get the modules from db instead of using the ModuleDiscoveryResult returned by
    // moduleDiscoveryHandler.updateModules() in order to retrieve the ids of newly discovered modules
    Set<Module> activeModules = moduleService.getByBranch(branch.getId().get()).stream().filter(Module::isActive).collect(Collectors.toSet());

    RepositoryBuild launching = queued.toBuilder()
        .setStartTimestamp(Optional.of(System.currentTimeMillis()))
        .setState(State.LAUNCHING)
        .setCommitInfo(Optional.of(commitInfo))
        .setSha(Optional.of(commitInfo.getCurrent().getId()))
        .setDependencyGraph(Optional.of(dependenciesService.buildDependencyGraph(branch, activeModules)))
        .build();
    LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
    repositoryBuildService.begin(launching);
  }

  @VisibleForTesting
  CommitInfo calculateCommitInfoForBuild(
      GitInfo gitInfo,
      RepositoryBuild queuedBuild,
      Optional<RepositoryBuild> previousBuild) throws IOException, NonRetryableBuildException {
    LOG.info("Trying to fetch current sha for branch {}/{}", gitInfo.getRepository(), gitInfo.getBranch());

    final GHRepository repository;
    try {
      repository = gitHubHelper.repositoryFor(gitInfo);
    } catch (FileNotFoundException e) {
      throw new NonRetryableBuildException("Couldn't find repository " + gitInfo.getFullRepositoryName(), e);
    }

    Optional<Commit> lastCommitInPreviousBuild = getCommitFromRepositoryBuild(previousBuild);

    // Inter project builds re-build the project using the commit of the last build if available
    // otherwise we fall back to using the newest commit available
    if (queuedBuild.getBuildTrigger().getType() == INTER_PROJECT && lastCommitInPreviousBuild.isPresent()) {
      return new CommitInfo(lastCommitInPreviousBuild.get(), lastCommitInPreviousBuild, ImmutableList.of(), false);
    }

    // Resolve newest sha
    Optional<String> sha = gitHubHelper.shaFor(repository, gitInfo);
    if (!sha.isPresent()) {
      String message = String.format("Couldn't find branch %s for repository %s", gitInfo.getBranch(), gitInfo.getFullRepositoryName());
      throw new NonRetryableBuildException(message);
    } else {
      LOG.info("Found sha {} for branch {}/{}", sha.get(), gitInfo.getRepository(), gitInfo.getBranch());

      Commit currentCommit = gitHubHelper.toCommit(repository.getCommit(sha.get()));
      return gitHubHelper.commitInfoFor(repository, currentCommit, lastCommitInPreviousBuild);
    }
  }

  private static Optional<Commit> getCommitFromRepositoryBuild(Optional<RepositoryBuild> build) {
    if (build.isPresent() && build.get().getCommitInfo() != null && build.get().getCommitInfo().isPresent()) {
      return Optional.of(build.get().getCommitInfo().get().getCurrent());
    } else {
      return Optional.absent();
    }
  }
}
