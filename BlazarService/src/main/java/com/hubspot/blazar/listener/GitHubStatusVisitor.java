package com.hubspot.blazar.listener;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.util.BlazarUrlHelper;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class GitHubStatusVisitor implements RepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubStatusVisitor.class);

  private final BranchService branchService;
  private final GitHubHelper gitHubHelper;
  private final BlazarUrlHelper blazarUrlHelper;

  @Inject
  public GitHubStatusVisitor(BranchService branchService,
                             GitHubHelper gitHubHelper,
                             BlazarUrlHelper blazarUrlHelper) {
    this.branchService = branchService;
    this.gitHubHelper = gitHubHelper;
    this.blazarUrlHelper = blazarUrlHelper;
  }

  @Override
  public void visit(RepositoryBuild build) throws Exception {
    // Can't set GitHub status without a sha
    if (!build.getSha().isPresent()) {
      return;
    }

    GitInfo gitInfo = branchService.get(build.getBranchId()).get();
    String url = blazarUrlHelper.getBlazarUiLink(build);

    GHCommitState state = toGHCommitState(build.getState());
    String sha = build.getSha().get();
    String description = getStateDescription(build.getState());

    final GHRepository repository;
    try {
      repository = gitHubHelper.repositoryFor(gitInfo);
    } catch (FileNotFoundException e) {
      LOG.warn("Couldn't find repository {}", gitInfo.getFullRepositoryName(), e);
      return;
    }

    LOG.info("Setting status of commit {} to {} for build {}", sha.substring(0, 8), state, build.getId().get());
    try {
      repository.createCommitStatus(sha, state, url, description, "Blazar");
    } catch (IOException e) {
      LOG.error("Error setting status of commit {} to {} for build {}", sha.substring(0, 8), state, build.getId().get(), e);
    }
  }

  private static String getStateDescription(RepositoryBuild.State state) {
    switch (state) {
      case LAUNCHING:
        return "The build is launching";
      case IN_PROGRESS:
        return "The build is in progress";
      case SUCCEEDED:
        return "The build succeeded!";
      case FAILED:
        return "The build failed";
      case CANCELLED:
        return "The build was cancelled";
      case UNSTABLE:
        return "The build succeeded, but other modules are in a failed state";
      default:
        throw new IllegalArgumentException("Unexpected build state: " + state);
    }
  }

  private static GHCommitState toGHCommitState(RepositoryBuild.State state) {
    switch (state) {
      case LAUNCHING:
      case IN_PROGRESS:
        return GHCommitState.PENDING;
      case SUCCEEDED:
        return GHCommitState.SUCCESS;
      case FAILED:
      case UNSTABLE:
        return GHCommitState.FAILURE;
      case CANCELLED:
        return GHCommitState.ERROR;
      default:
        throw new IllegalArgumentException("Unexpected build state: " + state);
    }
  }
}
