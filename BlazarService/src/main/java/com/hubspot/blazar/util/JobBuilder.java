package com.hubspot.blazar.util;

import com.google.common.base.Preconditions;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.service.BuildService;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Singleton
public class JobBuilder {
  private final BuildService buildService;
  private final Map<String, GitHub> gitHubByHost;

  @Inject
  public JobBuilder(BuildService buildService, Map<String, GitHub> gitHubByHost) {
    this.buildService = buildService;
    this.gitHubByHost = gitHubByHost;
  }

  public void triggerBuild(BuildDefinition definition) throws IOException {
    BuildState buildState = buildService.enqueue(definition.getModule());
    Preconditions.checkState(buildState.getPendingBuild().isPresent());

    if (!buildState.getInProgressBuild().isPresent()) {
      Build queued = buildState.getPendingBuild().get();

      Build launching = queued.withSha(currentSha(definition.getGitInfo()))
          .withState(State.LAUNCHING)
          .withStartTimestamp(System.currentTimeMillis());

      buildService.begin(launching);

      // TODO launch singularity task
    }
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
