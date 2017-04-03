package com.hubspot.blazar.service;

import static org.assertj.core.api.Fail.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildMetadata;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

public class TestUtils {
  private final InterProjectBuildService interProjectBuildService;
  private final RepositoryBuildService repositoryBuildService;
  private static final long BUILD_WAIT_TIME = 1000;
  private static final long BUILD_WAIT_MAX_COUNT = 10;

  public static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

  @Inject
  public TestUtils (InterProjectBuildService interProjectBuildService,
                    RepositoryBuildService repositoryBuildService) {
    this.interProjectBuildService = interProjectBuildService;
    this.repositoryBuildService = repositoryBuildService;
  }

  public InterProjectBuild runInterProjectBuild(int rootModuleId, Optional<BuildMetadata> triggerOptional) throws InterruptedException {
    BuildMetadata trigger;
    if (triggerOptional.isPresent()) {
      trigger = triggerOptional.get();
    } else {
      trigger = BuildMetadata.manual(Optional.of("testUser"));
    }
    LOG.info("Starting inter-project-build for id {}", rootModuleId);
    InterProjectBuild build = InterProjectBuild.getQueuedBuild(Sets.newHashSet(rootModuleId), trigger);
    long id = interProjectBuildService.enqueue(build);
    return waitForInterProjectBuild(interProjectBuildService.getWithId(id).get());
  }

  public RepositoryBuild runDefaultRepositoryBuild(GitInfo gitInfo) throws InterruptedException {
    long id = repositoryBuildService.enqueue(gitInfo, BuildMetadata.push("testUser"), BuildOptions.defaultOptions());
    RepositoryBuild build = repositoryBuildService.get(id).get();
    return waitForRepositoryBuild(build);
  }

  public RepositoryBuild runAndWaitForRepositoryBuild(GitInfo gitInfo, BuildMetadata buildMetadata, BuildOptions buildOptions) {
    long id = repositoryBuildService.enqueue(gitInfo, buildMetadata, buildOptions);
    RepositoryBuild build = repositoryBuildService.get(id).get();
    return waitForRepositoryBuild(build);
  }

  public InterProjectBuild waitForInterProjectBuild(InterProjectBuild build) {
    int count = 0;
    while (!build.getState().isFinished()) {
      if (count > BUILD_WAIT_MAX_COUNT) {
        fail(String.format("Build %s took more than 10s to complete", build));
      }
      count++;
      try {
        LOG.debug("Waiting for {} to complete", build);
        Thread.sleep(BUILD_WAIT_TIME);
      } catch (InterruptedException e) {
        LOG.error("Got interrupted while waiting for build", e);
        Thread.interrupted();
      }
      build = interProjectBuildService.getWithId(build.getId().get()).get();
    }
    return build;
  }

  public RepositoryBuild waitForRepositoryBuild(RepositoryBuild build) {
    int count = 0;
    while (!build.getState().isComplete()) {
      if (count > BUILD_WAIT_MAX_COUNT) {
        fail(String.format("Build %s took more than 10s to complete", build));
      }
      count++;
      try {
        LOG.debug("Waiting for {} to complete", build);
        Thread.sleep(BUILD_WAIT_TIME);
      } catch (InterruptedException e) {
        LOG.error("Got interrupted while waiting for build", e);
        Thread.interrupted();
      }
      build = repositoryBuildService.get(build.getId().get()).get();
    }
    return build;
  }
}
