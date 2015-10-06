package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;

@Singleton
public class GitHubStatusHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubStatusHandler.class);

  private final Map<String, GitHub> gitHubByHost;
  private final BuildDefinitionService buildDefinitionService;

  @Inject
  public GitHubStatusHandler(EventBus eventBus, Map<String, GitHub> gitHubByHost, BuildDefinitionService buildDefinitionService) {
    this.gitHubByHost = gitHubByHost;
    this.buildDefinitionService = buildDefinitionService;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildStateChange(Build build) throws IOException {
    BuildDefinition definition = buildDefinitionService.getByModule(build.getModuleId()).get();
    if (!shouldUpdateStatus(definition, build)) {
      LOG.info("Not setting status for build {} with state {}", build.getId().get(), build.getState());
      return;
    }

    GitInfo git = definition.getGitInfo();
    Module module = definition.getModule();

    String url = UriBuilder.fromUri("https://tools.hubteam.com/blazar/builds")
        .segment(git.getHost())
        .segment(git.getOrganization())
        .segment(git.getRepository())
        .segment(git.getBranch())
        .segment(module.getName())
        .segment(String.valueOf(build.getBuildNumber()))
        .build()
        .toString();
    GHCommitState state = toGHCommitState(build.getState());
    String sha = build.getSha().get();
    String description = getStateDescription(build.getState());
    String context = module.getName() + "/blazar";

    GitHub gitHub = Preconditions.checkNotNull(gitHubByHost.get(git.getHost()));

    final GHRepository repository;
    try {
      repository = gitHub.getRepository(git.getFullRepositoryName());
    } catch (FileNotFoundException e) {
      LOG.warn("Couldn't find repository {}", git.getFullRepositoryName(), e);
      return;
    }

    LOG.info("Setting status of commit {} to {} for build {}", sha, state, build.getId().get());
    repository.createCommitStatus(sha, state, url, description, context);
  }

  private boolean shouldUpdateStatus(BuildDefinition definition, Build build) {
    Optional<BuildConfig> buildConfig = build.getBuildConfig();
    if (build.getState() == State.QUEUED) {
      return false;
    } else if (buildConfig.isPresent() && manualBuildSpecified(buildConfig.get())) {
      return true;
    } else {
      String whitelist = Objects.firstNonNull(System.getenv("BUILD_WHITELIST"), "");
      List<String> whitelistedRepos= Splitter.on(',').trimResults().omitEmptyStrings().splitToList(whitelist);

      return whitelistedRepos.contains(definition.getGitInfo().getRepository());
    }
  }

  private static boolean manualBuildSpecified(BuildConfig buildConfig) {
    return !buildConfig.getCmds().isEmpty() || buildConfig.getBuildpack().isPresent();
  }

  private static String getStateDescription(State state) {
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

  private static GHCommitState toGHCommitState(State state) {
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
