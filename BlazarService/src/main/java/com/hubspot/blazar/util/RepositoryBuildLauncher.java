package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;

@Singleton
public class RepositoryBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final BranchService branchService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public RepositoryBuildLauncher(RepositoryBuildService repositoryBuildService,
                                 BranchService branchService,
                                 GitHubHelper gitHubHelper,
                                 EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.branchService = branchService;
    this.gitHubHelper = gitHubHelper;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(RepositoryBuild build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    final GitInfo gitInfo;
    final RepositoryBuild buildToLaunch;
    final Optional<RepositoryBuild> previousBuild;
    if (build.getState() == State.QUEUED) {
      BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(build.getBranchId());
      if (build.getBuildNumber() != buildNumbers.getPendingBuildNumber().or(-1)) {
        LOG.info("Build {} is no longer pending for branch {}, not launching", build.getId().get(), build.getBranchId());
        return;
      } else if (buildNumbers.getInProgressBuildNumber().isPresent()) {
        LOG.info("In progress build for branch {}, not launching build {}", build.getBranchId(), build.getId().get());
        return;
      } else {
        LOG.info("No in progress build for branch {}, going to launch build {}", build.getBranchId(), build.getId().get());
        gitInfo = branchService.get(build.getBranchId()).get();
        buildToLaunch = build;
        if (buildNumbers.getLastBuildId().isPresent()) {
          previousBuild = repositoryBuildService.get(buildNumbers.getLastBuildId().get());
          Preconditions.checkState(previousBuild.isPresent(), "Could not find build %s for branch %s", buildNumbers.getLastBuildId().get(), build.getBranchId());
        } else {
          previousBuild = Optional.absent();
        }
      }
    } else if (build.getState().isComplete()) {
      BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(build.getBranchId());

      if (buildNumbers.getPendingBuildId().isPresent() && !buildNumbers.getInProgressBuildId().isPresent() && build.getBuildNumber() == buildNumbers.getLastBuildNumber().or(-1)) {
        LOG.info("Pending build for branch {}, going to launch build {}", build.getBranchId(), buildNumbers.getPendingBuildId().get());
        gitInfo = branchService.get(build.getBranchId()).get();;
        buildToLaunch = repositoryBuildService.get(buildNumbers.getPendingBuildId().get()).get();
        previousBuild = Optional.of(build);
      } else {
        LOG.info("No pending build for branch {}", build.getBranchId());
        return;
      }
    } else {
      return;
    }

    try {
      startBuild(gitInfo, buildToLaunch, previousBuild);
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", buildToLaunch.getId().get(), e);
      repositoryBuildService.fail(buildToLaunch);
    }
  }

  private synchronized void startBuild(GitInfo gitInfo, RepositoryBuild queued, Optional<RepositoryBuild> previous) throws Exception {
    CommitInfo commitInfo = commitInfo(gitInfo, commit(previous));

    RepositoryBuild launching = queued.withStartTimestamp(System.currentTimeMillis())
        .withState(State.LAUNCHING)
        .withCommitInfo(commitInfo);

    LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
    repositoryBuildService.begin(launching);
  }

  private CommitInfo commitInfo(GitInfo gitInfo, Optional<Commit> previousCommit) throws IOException, NonRetryableBuildException {
    LOG.info("Trying to fetch current sha for branch {}/{}", gitInfo.getRepository(), gitInfo.getBranch());

    final GHRepository repository;
    try {
      repository = gitHubHelper.repositoryFor(gitInfo);
    } catch (FileNotFoundException e) {
      throw new NonRetryableBuildException("Couldn't find repository " + gitInfo.getFullRepositoryName(), e);
    }

    Optional<String> sha = gitHubHelper.shaFor(repository, gitInfo);
    if (!sha.isPresent()) {
      String message = String.format("Couldn't find branch %s for repository %s", gitInfo.getBranch(), gitInfo.getFullRepositoryName());
      throw new NonRetryableBuildException(message);
    } else {
      LOG.info("Found sha {} for branch {}/{}", sha.get(), gitInfo.getRepository(), gitInfo.getBranch());

      Commit currentCommit = gitHubHelper.toCommit(repository.getCommit(sha.get()));
      return gitHubHelper.commitInfoFor(repository, currentCommit, previousCommit);
    }
  }

  private static Optional<Commit> commit(Optional<RepositoryBuild> build) {
    if (build.isPresent()) {
      return Optional.of(build.get().getCommitInfo().get().getCurrent());
    } else {
      return Optional.absent();
    }
  }
}
