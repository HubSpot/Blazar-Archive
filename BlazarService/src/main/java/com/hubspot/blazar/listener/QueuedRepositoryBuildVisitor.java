package com.hubspot.blazar.listener;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.blazar.util.RepositoryBuildLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    if (build.getBuildNumber() != buildNumbers.getPendingBuildNumber().or(-1)) {
      LOG.info("Build {} is no longer pending for branch {}, not launching", build.getId().get(), build.getBranchId());
    } else if (buildNumbers.getInProgressBuildId().isPresent()) {
      LOG.info("In progress build for branch {}, not launching pending build {}", build.getBranchId(), build.getId().get());
    } else {
      LOG.info("Going to launch pending build {} for branch {}", build.getId().get(), build.getBranchId());
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
