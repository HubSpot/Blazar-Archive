package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.Set;

public class ModuleBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final BranchService branchService;
  private final ModuleService moduleService;
  private final ModuleDiscovery moduleDiscovery;
  private final GitHubHelper gitHubHelper;

  @Inject
  public ModuleBuildLauncher(RepositoryBuildService repositoryBuildService,
                             BranchService branchService,
                             ModuleService moduleService,
                             ModuleDiscovery moduleDiscovery,
                             GitHubHelper gitHubHelper,
                             EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleDiscovery = moduleDiscovery;
    this.gitHubHelper = gitHubHelper;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(RepositoryBuild build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    if (build.getState() == State.LAUNCHING) {
      CommitInfo commitInfo = build.getCommitInfo().get();
      GitInfo gitInfo = branchService.get(build.getBranchId()).get().withBranch(commitInfo.getCurrent().getId());

      final Set<Module> modules;
      if (commitInfo.isTruncated() || moduleDiscovery.shouldRediscover(gitInfo, commitInfo)) {
        modules = moduleService.setModules(gitInfo, moduleDiscovery.discover(gitInfo));
      } else {
        modules = moduleService.getByBranch(build.getBranchId());
      }
    }
  }

  private void triggerBuilds(CommitInfo commitInfo, GitInfo gitInfo, Set<Module> modules) throws IOException {
    Set<Module> toBuild = new HashSet<>();
    if (commitInfo.isTruncated()) {
      toBuild = modules;
    } else {
      for (String path : gitHubHelper.affectedPaths(commitInfo)) {
        for (Module module : modules) {
          if (module.contains(FileSystems.getDefault().getPath(path))) {
            toBuild.add(module);
          }
        }
      }
    }

    triggerBuilds(gitInfo, toBuild);
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
