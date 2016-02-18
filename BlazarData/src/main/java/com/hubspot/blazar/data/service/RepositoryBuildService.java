package com.hubspot.blazar.data.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.util.BuildNumbers;

@Singleton
public class RepositoryBuildService {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryBuildService.class);

  private final RepositoryBuildDao repositoryBuildDao;
  private final BranchDao branchDao;
  private final EventBus eventBus;

  @Inject
  public RepositoryBuildService(RepositoryBuildDao repositoryBuildDao, BranchDao branchDao, EventBus eventBus) {
    this.repositoryBuildDao = repositoryBuildDao;
    this.branchDao = branchDao;
    this.eventBus = eventBus;
  }

  public Optional<RepositoryBuild> get(long id) {
    return repositoryBuildDao.get(id);
  }

  public List<RepositoryBuild> getByBranch(int branchId) {
    return repositoryBuildDao.getByBranch(branchId);
  }

  public Optional<RepositoryBuild> getByBranchAndNumber(int branchId, int buildNumber) {
    return repositoryBuildDao.getByBranchAndNumber(branchId, buildNumber);
  }

  public BuildNumbers getBuildNumbers(int branchId) {
    return repositoryBuildDao.getBuildNumbers(branchId);
  }

  public long enqueue(GitInfo gitInfo, BuildTrigger trigger, BuildOptions buildOptions) {
    BuildNumbers buildNumbers = getBuildNumbers(gitInfo.getId().get());

    if (buildNumbers.getPendingBuildId().isPresent()) {
      long pendingBuildId = buildNumbers.getPendingBuildId().get();
      LOG.info("Not enqueuing build for repository {}, pending build {} already exists", gitInfo.getId().get(), pendingBuildId);
      return pendingBuildId;
    } else {
      int nextBuildNumber = buildNumbers.getNextBuildNumber();
      LOG.info("Enqueuing build for repository {} with build number {}", gitInfo.getId().get(), nextBuildNumber);
      RepositoryBuild build = RepositoryBuild.queuedBuild(gitInfo, trigger, nextBuildNumber, buildOptions);
      build = enqueue(build);
      LOG.info("Enqueued build for repository {} with id {}", gitInfo.getId().get(), build.getId().get());
      return build.getId().get();
    }
  }

  @Transactional
  protected RepositoryBuild enqueue(RepositoryBuild build) {
    long id = repositoryBuildDao.enqueue(build);
    build = build.withId(id);

    checkAffectedRowCount(branchDao.updatePendingBuild(build));

    eventBus.post(build);

    return build;
  }

  @Transactional
  public void begin(RepositoryBuild build) {
    beginNoPublish(build);

    eventBus.post(build);
  }

  @Transactional
  void beginNoPublish(RepositoryBuild build) {
    Preconditions.checkArgument(build.getStartTimestamp().isPresent());

    checkAffectedRowCount(repositoryBuildDao.begin(build));
    checkAffectedRowCount(branchDao.updateInProgressBuild(build));
  }

  @Transactional
  public void update(RepositoryBuild build) {
    if (build.getState().isComplete()) {
      Preconditions.checkArgument(build.getEndTimestamp().isPresent());

      checkAffectedRowCount(repositoryBuildDao.complete(build));
      checkAffectedRowCount(branchDao.updateLastBuild(build));
    } else {
      checkAffectedRowCount(repositoryBuildDao.update(build));
    }

    eventBus.post(build);
  }

  @Transactional
  public void fail(RepositoryBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState() == State.QUEUED) {
      beginNoPublish(build.withState(State.LAUNCHING).withStartTimestamp(System.currentTimeMillis()));
    }

    update(build.withState(State.FAILED).withEndTimestamp(System.currentTimeMillis()));
  }

  @Transactional
  public void cancel(RepositoryBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState() == State.QUEUED) {
      checkAffectedRowCount(repositoryBuildDao.delete(build));
      checkAffectedRowCount(branchDao.deletePendingBuild(build));
    } else {
      update(build.withState(State.CANCELLED).withEndTimestamp(System.currentTimeMillis()));
    }
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }
}
