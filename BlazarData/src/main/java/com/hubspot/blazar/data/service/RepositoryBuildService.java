package com.hubspot.blazar.data.service;

import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.util.BuildNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RepositoryBuildService {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryBuildService.class);

  private final RepositoryBuildDao repositoryBuildDao;

  @Inject
  public RepositoryBuildService(RepositoryBuildDao repositoryBuildDao) {
    this.repositoryBuildDao = repositoryBuildDao;
  }

  public void enqueue(GitInfo gitInfo) {
    BuildNumbers buildNumbers = repositoryBuildDao.getBuildNumbers(gitInfo);

    if (buildNumbers.getPendingBuildNumber().isPresent()) {
      int pendingBuildNumber = buildNumbers.getPendingBuildNumber().get();
      LOG.info("Not enqueuing build for repository {}, pending build {} already exists", gitInfo.getId().get(), pendingBuildNumber);
    } else {
      final int nextBuildNumber;
      if (buildNumbers.getInProgressBuildNumber().isPresent()) {
        nextBuildNumber = buildNumbers.getInProgressBuildNumber().get() + 1;
      } else if (buildNumbers.getLastBuildNumber().isPresent()) {
        nextBuildNumber = buildNumbers.getLastBuildNumber().get() + 1;
      } else {
        nextBuildNumber = 1;
      }

      LOG.info("Enqueuing build for repository {} with build number {}", gitInfo.getId().get(), nextBuildNumber);
      Build build = Build.queuedBuild(definition.getModule(), nextBuildNumber);
      build = enqueue(build);
      LOG.info("Enqueued build for repository {} with id {}", gitInfo.getId().get(), build.getId().get());
    }
  }
}
