package com.hubspot.blazar.listener;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.blazar.util.RepositoryBuildLauncher;

@Singleton
public class QueuedRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(QueuedRepositoryBuildVisitor.class);

  private final RepositoryBuildService repositoryBuildService;
  private final RepositoryBuildLauncher buildLauncher;

  @Inject
  public QueuedRepositoryBuildVisitor(RepositoryBuildService repositoryBuildService,
                                      RepositoryBuildLauncher buildLauncher) {
    this.repositoryBuildService = repositoryBuildService;
    this.buildLauncher = buildLauncher;
  }

  @Override
  protected void visitQueued(RepositoryBuild build) throws Exception {
    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(build.getBranchId());

    int pendingBuildNumber = buildNumbers.getPendingBuildNumber().or(-1);
    int inProgressBuildNumber = buildNumbers.getInProgressBuildNumber().or(-1);

    if (build.getBuildNumber() != pendingBuildNumber) {
      // now that we allow multiple queued builds this will get hit
      LOG.info("Queued Repository Build {} is not marked as the pending build for branch {}, will not launch it yet. The currently pending build is {}.",
          build.getId().get(), build.getBranchId(), pendingBuildNumber);
    } else if (buildNumbers.getInProgressBuildId().isPresent()) {
      LOG.info("Queued repository build {} (build# {}) is the next pending build for branch {} but repository build {} (build# {}) is in progress, will not launch queued build yet",
          build.getId().get(), build.getBuildNumber(), build.getBranchId(), buildNumbers.getInProgressBuildId().get(), inProgressBuildNumber);
    } else {
      LOG.info("Queued Repository Build {} (build# {}) is marked as the next pending build for branch {} and no other build is currently in progress, will launch this build now.",
          build.getId().get(), build.getBuildNumber(), build.getBranchId());
      final Optional<RepositoryBuild> previous;
      if (buildNumbers.getLastBuildId().isPresent()) {
        previous = Optional.of(repositoryBuildService.get(buildNumbers.getLastBuildId().get()).get());
      } else {
        previous = Optional.absent();
      }
      buildLauncher.launch(build, previous);
    }
  }
}
