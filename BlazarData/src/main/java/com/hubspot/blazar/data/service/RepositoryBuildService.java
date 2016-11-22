package com.hubspot.blazar.data.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;
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

  public Optional<RepositoryBuild> getPreviousBuild(RepositoryBuild build) {
    return repositoryBuildDao.getPreviousBuild(build);
  }

  public long enqueue(GitInfo gitInfo, BuildTrigger trigger, BuildOptions buildOptions) {
    int branchId = gitInfo.getId().get();

    // determine build number first (unique index will prevent concurrent modification)
    List<RepositoryBuild> queuedBuilds = repositoryBuildDao.getByBranchAndState(branchId, State.QUEUED);
    int nextBuildNumber = determineNextBuildNumber(branchId, queuedBuilds);

    // check for existing queued build triggered by push event
    if (trigger.getType() == BuildTrigger.Type.PUSH) {
      for (RepositoryBuild build : queuedBuilds) {
        if (build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH) {
          long existingBuildId = build.getId().get();
          LOG.info("Not enqueuing build for push to {}, pending build {} already exists", branchId, existingBuildId);
          return existingBuildId;
        }
      }
    }

    RepositoryBuild build = RepositoryBuild.queuedBuild(gitInfo, trigger, nextBuildNumber, buildOptions);
    LOG.info("Enqueuing build for repository {} with build number {}", branchId, nextBuildNumber);
    // if no queued builds, we expect our update to succeed otherwise it shouldn't
    int expectedUpdateCount = queuedBuilds.isEmpty() ? 1 : 0;
    build = enqueue(build, expectedUpdateCount);
    LOG.info("Enqueued build for repository {} with id {}", branchId, build.getId().get());
    return build.getId().get();
  }

  @Transactional
  protected RepositoryBuild enqueue(RepositoryBuild build, int expectedUpdateCount) {
    long id = repositoryBuildDao.enqueue(build);
    build = build.toBuilder().setId(Optional.of(id)).build();

    checkAffectedRowCount(branchDao.updatePendingBuild(build), expectedUpdateCount);

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
    // need to move the next queued build into the pending slot
    Optional<RepositoryBuild> nextQueuedBuild = nextQueuedBuild(build.getBranchId());
    if (nextQueuedBuild.isPresent()) {
      checkAffectedRowCount(branchDao.updateInProgressBuild(build, nextQueuedBuild.get()));

      eventBus.post(nextQueuedBuild.get());
    } else {
      checkAffectedRowCount(branchDao.updateInProgressBuild(build));
    }
  }

  @Transactional
  public void update(RepositoryBuild build) {
    if (build.getState().isComplete()) {
      Preconditions.checkArgument(build.getEndTimestamp().isPresent());

      checkAffectedRowCount(repositoryBuildDao.complete(build));
      checkAffectedRowCount(branchDao.updateLastBuild(build));
    } else {
      checkAffectedRowCount(repositoryBuildDao.update(build));
      // touch the updatedTimestamp on the branch so the cache knows it changed
      checkAffectedRowCount(branchDao.touch(build.getBranchId()));
    }

    eventBus.post(build);
  }

  @Transactional
  public void fail(RepositoryBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState() == State.QUEUED) {
      beginNoPublish(build.toBuilder().setState(State.LAUNCHING).setStartTimestamp(Optional.of(System.currentTimeMillis())).build());
    }

    update(build.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(System.currentTimeMillis())).build());
  }

  public void cancel(RepositoryBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState() == State.QUEUED) {
      deleteQueuedBuild(build);
    } else {
      update(build.toBuilder().setState(State.CANCELLED).setEndTimestamp(Optional.of(System.currentTimeMillis())).build());
    }
  }

  @Transactional
  void deleteQueuedBuild(RepositoryBuild build) {
    // call this method before deleting the row
    boolean isThePendingBuild = isThePendingBuild(build);
    checkAffectedRowCount(repositoryBuildDao.delete(build));
    if (isThePendingBuild) {
      // need to move the next queued build into the pending slot
      Optional<RepositoryBuild> nextQueuedBuild = nextQueuedBuild(build.getBranchId());
      if (nextQueuedBuild.isPresent()) {
        checkAffectedRowCount(branchDao.updatePendingBuild(build, nextQueuedBuild.get()));

        eventBus.post(nextQueuedBuild.get());
      } else {
        checkAffectedRowCount(branchDao.deletePendingBuild(build));
      }
    } else {
      // this shouldn't update any rows
      checkAffectedRowCount(branchDao.deletePendingBuild(build), 0);
    }
  }

  private boolean isThePendingBuild(RepositoryBuild build) {
    BuildNumbers buildNumbers = getBuildNumbers(build.getBranchId());
    return buildNumbers.getPendingBuildId().equals(build.getId());
  }

  private Optional<RepositoryBuild> nextQueuedBuild(int branchId) {
    List<RepositoryBuild> queuedBuilds = repositoryBuildDao.getByBranchAndState(branchId, State.QUEUED);
    if (queuedBuilds.isEmpty()) {
      return Optional.absent();
    } else {
      return Optional.of(Collections.min(queuedBuilds, new Comparator<RepositoryBuild>() {

        @Override
        public int compare(RepositoryBuild build1, RepositoryBuild build2) {
          return Ints.compare(build1.getBuildNumber(), build2.getBuildNumber());
        }
      }));
    }
  }

  private int determineNextBuildNumber(int branchId, List<RepositoryBuild> queuedBuilds) {
    if (queuedBuilds.isEmpty()) {
      BuildNumbers buildNumbers = getBuildNumbers(branchId);
      if (buildNumbers.getInProgressBuildNumber().isPresent()) {
        return buildNumbers.getInProgressBuildNumber().get() + 1;
      } else if (buildNumbers.getLastBuildNumber().isPresent()) {
        return buildNumbers.getLastBuildNumber().get() + 1;
      } else {
        return 1;
      }
    } else {
      int max = 0;
      for (RepositoryBuild build : queuedBuilds) {
        max = Math.max(max, build.getBuildNumber());
      }

      return max + 1;
    }
  }

  private static void checkAffectedRowCount(int affectedRows) {
    checkAffectedRowCount(affectedRows, 1);
  }

  private static void checkAffectedRowCount(int affectedRows, int expectedAffectedRows) {
    Preconditions.checkState(affectedRows == expectedAffectedRows, "Expected to update %s rows but updated %s", expectedAffectedRows, affectedRows);
  }
}
