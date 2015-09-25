package com.hubspot.blazar.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.data.service.BuildDefinitionService;

import io.dropwizard.Configuration;

@Singleton
public class GithubStatusHandler {

  private final Map<String, GitHub> gitHubByHost;
  private final BuildDefinitionService buildDefinitionService;
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GithubStatusHandler.class);

  @Inject
  public GithubStatusHandler (EventBus eventBus, Map<String, GitHub> gitHubByHost, BuildDefinitionService buildDefinitionService) {
    this.gitHubByHost = gitHubByHost;
    this.buildDefinitionService = buildDefinitionService;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildStateChange(Build build) throws IOException {
    LOG.info("Got event for a buildId: {}", build.getBuildNumber());
    if (build.getState() == State.QUEUED) {
      return;
    }

    BuildDefinition definition = buildDefinitionService.getByModule(build.getModuleId()).get();
    String whitelist = Objects.firstNonNull(System.getenv("BUILD_WHITELIST"), "");
    List<String> whitelistedRepos= Splitter.on(',').omitEmptyStrings().splitToList(whitelist);

    if (!whitelistedRepos.contains(definition.getGitInfo().getRepository())) {
      return;
    }

    String uiHost = "https://tools.hubteam.com";
    String appRoot = "blazar";
    String gitHost = definition.getGitInfo().getHost();
    String gitOrg = definition.getGitInfo().getOrganization();
    String gitRepo = definition.getGitInfo().getRepository();
    String gitBranch  = definition.getGitInfo().getBranch();
    String moduleName = definition.getModule().getName();
    String buildNumber = Integer.toString(build.getBuildNumber());

    String url = uiHost + "/" + appRoot + "/builds" + "/" + gitHost + "/" + gitOrg + "/" + gitRepo + "/" + gitBranch + "/" + moduleName + "/" + buildNumber;
    GHCommitState state = toGHCommitState(build.getState());
    String sha = build.getSha().get();
    String description = getStateDescription(build.getState());
    String context = "CI-blazar";

    GitHub gitHubApi = gitHubByHost.get(gitHost);

    GHRepository gitHubApiRepo = gitHubApi.getRepository(definition.getGitInfo().getFullRepositoryName());
    LOG.info("Setting status of commit {} on module {}:{} (moduleId: {}) to {}", sha, definition.getGitInfo().getFullRepositoryName(), gitBranch, build.getModuleId(), description);
    gitHubApiRepo.createCommitStatus(sha, state, url, description, context);
  }

  private String getStateDescription (State state) {
     switch (state) {
      case QUEUED:
        throw new IllegalArgumentException("Queued builds have no analagous GH Commit State");
      case LAUNCHING:
        return "The build is launching, we'll be building it soon!";
      case IN_PROGRESS:
        return "We're building your project!";
      case FAILED:
        return "The build failed.";
      case SUCCEEDED:
        return "The build succeeded!";
      case CANCELLED:
        return "Build was cancelled.";
       default:
         throw new IllegalArgumentException(String.format("No message for the build state: %s", state.toString()));
    }
  }

  private GHCommitState toGHCommitState (State state) {
    switch (state) {
      case QUEUED:
        throw new IllegalArgumentException("Queued builds have no analagous GH Commit State");
      case LAUNCHING:
        return GHCommitState.PENDING;
      case IN_PROGRESS:
        return GHCommitState.PENDING;
      case FAILED:
        return GHCommitState.FAILURE;
      case SUCCEEDED:
        return GHCommitState.SUCCESS;
      case CANCELLED:
        return GHCommitState.ERROR;
      default:
        throw new IllegalArgumentException(String.format("Could not match state %s to a gihub state", state.toString()));
    }

  }
}
