package com.hubspot.blazar.util;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.BuildStateService;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Singleton
public class BuildLauncher {
  private final BuildStateService buildStateService;
  private final BuildService buildService;
  private final Map<String, GitHub> gitHubByHost;

  @Inject
  public BuildLauncher(BuildStateService buildStateService,
                       BuildService buildService,
                       Map<String, GitHub> gitHubByHost,
                       EventBus eventBus) {
    this.buildStateService = buildStateService;
    this.buildService = buildService;
    this.gitHubByHost = gitHubByHost;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(Build build) throws IOException {
    final BuildDefinition buildDefinition;
    final Build buildToLaunch;
    if (build.getState() == State.QUEUED) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId());
      if (!buildState.getInProgressBuild().isPresent()) {
        buildDefinition = buildState;
        buildToLaunch = build;
      } else {
        return;
      }
    } else if (build.getState().isComplete()) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId());
      if (buildState.getPendingBuild().isPresent()) {
        buildDefinition = buildState;
        buildToLaunch = buildState.getPendingBuild().get();
      } else {
        return;
      }
    } else {
      return;
    }

    startBuild(buildDefinition, buildToLaunch);
  }

  public BuildState triggerBuild(BuildDefinition buildDefinition) {
    return buildService.enqueue(buildDefinition);
  }

  private void startBuild(BuildDefinition buildDefinition, Build queued) throws IOException {
    String sha = currentSha(buildDefinition.getGitInfo());
    Build launching = queued.withStartTimestamp(System.currentTimeMillis()).withState(State.LAUNCHING).withSha(sha);

    buildService.begin(launching);
    //TODO launch the build
    System.out.println("Going to launch build " + launching.getId().get());
  }

  private String currentSha(GitInfo gitInfo) throws IOException {
    GitHub gitHub = gitHubFor(gitInfo);

    GHRepository repository = gitHub.getRepository(gitInfo.getFullRepositoryName());
    GHBranch branch = Preconditions.checkNotNull(repository.getBranches().get(gitInfo.getBranch()));
    return branch.getSHA1();
  }

  private GitHub gitHubFor(GitInfo gitInfo) {
    String host = gitInfo.getHost();

    return Preconditions.checkNotNull(gitHubByHost.get(host), "No GitHub found for host " + host);
  }
}
