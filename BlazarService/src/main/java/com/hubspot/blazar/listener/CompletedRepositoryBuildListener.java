package com.hubspot.blazar.listener;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.blazar.util.RepositoryBuildLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CompletedRepositoryBuildListener implements RepositoryBuildListener {
  private static final Logger LOG = LoggerFactory.getLogger(CompletedRepositoryBuildListener.class);

  private final RepositoryBuildService repositoryBuildService;
  private final RepositoryBuildLauncher buildLauncher;

  @Inject
  public CompletedRepositoryBuildListener(RepositoryBuildService repositoryBuildService,
                                          RepositoryBuildLauncher buildLauncher) {
    this.repositoryBuildService = repositoryBuildService;
    this.buildLauncher = buildLauncher;
  }

  @Override
  public void buildChanged(RepositoryBuild build) throws Exception {
    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(build.getBranchId());

    Optional<Long> pendingBuildId = buildNumbers.getPendingBuildId();
    if (!buildNumbers.getPendingBuildId().isPresent()) {
      LOG.info("No pending build for branch {}", build.getBranchId());
    } else if (buildNumbers.getInProgressBuildId().isPresent()) {
      LOG.info("In progress build for branch {}, not launching pending build {}", build.getBranchId(), pendingBuildId.get());
    } else {
      LOG.info("Going to launch pending build {} for branch {}", pendingBuildId.get(), build.getBranchId());
      RepositoryBuild toLaunch = repositoryBuildService.get(pendingBuildId.get()).get();
      Optional<RepositoryBuild> previous = Optional.of(build);
      buildLauncher.launch(toLaunch, previous);
    }
  }
}
