package com.hubspot.blazar.visitor.repositorybuild;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.util.BuildNumbers;

@Singleton
public class CompletedRepositoryBuildVisitor implements RepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(CompletedRepositoryBuildVisitor.class);

  private final RepositoryBuildService repositoryBuildService;
  private final EventBus eventBus;

  @Inject
  public CompletedRepositoryBuildVisitor(RepositoryBuildService repositoryBuildService,
                                         EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.eventBus = eventBus;
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (build.getState().isComplete()) {
      launchPendingBuild(build);
    }
  }

  private void launchPendingBuild(RepositoryBuild build) throws Exception {
    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(build.getBranchId());

    Optional<Long> pendingBuildId = buildNumbers.getPendingBuildId();
    if (pendingBuildId.isPresent()) {
      LOG.info("Posting event for pending build {} for branch {}", pendingBuildId.get(), build.getBranchId());
      RepositoryBuild toLaunch = repositoryBuildService.get(pendingBuildId.get()).get();
      eventBus.post(toLaunch);
    } else {
      LOG.info("No pending build for branch {}", build.getBranchId());
    }
  }
}
