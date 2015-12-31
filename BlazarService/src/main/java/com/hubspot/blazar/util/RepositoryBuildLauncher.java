package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.discovery.ModuleDiscovery;
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
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryBuildLauncher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final BranchService branchService;
  private final ModuleService moduleService;
  private final DependenciesService dependenciesService;
  private final ModuleDiscovery moduleDiscovery;
  private final GitHubHelper gitHubHelper;

  @Inject
  public RepositoryBuildLauncher(RepositoryBuildService repositoryBuildService,
                                 BranchService branchService,
                                 ModuleService moduleService,
                                 DependenciesService dependenciesService,
                                 ModuleDiscovery moduleDiscovery,
                                 GitHubHelper gitHubHelper) {
    this.repositoryBuildService = repositoryBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.dependenciesService = dependenciesService;
    this.moduleDiscovery = moduleDiscovery;
    this.gitHubHelper = gitHubHelper;
  }

  public void launch(RepositoryBuild queued, Optional<RepositoryBuild> previous) throws Exception {
    GitInfo gitInfo = branchService.get(queued.getBranchId()).get();;
    CommitInfo commitInfo = commitInfo(gitInfo, commit(previous));
    updateModules(gitInfo, commitInfo);

    RepositoryBuild launching = queued.withStartTimestamp(System.currentTimeMillis())
        .withState(State.LAUNCHING)
        .withCommitInfo(commitInfo)
        .withDependencyGraph(dependenciesService.buildDependencyGraph(gitInfo));
    LOG.info("L58: {}", queued.getBuildTrigger());
    LOG.info("L59: {}", launching.getBuildTrigger());
    LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
    repositoryBuildService.begin(launching);
  }

  private void updateModules(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    if (commitInfo.isTruncated() || moduleDiscovery.shouldRediscover(gitInfo, commitInfo)) {
      moduleService.setModules(gitInfo, moduleDiscovery.discover(gitInfo));
    }
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
