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
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Singleton
public class BuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final BuildStateService buildStateService;
  private final BuildService buildService;
  private final AsyncHttpClient asyncHttpClient;
  private final Map<String, GitHub> gitHubByHost;

  @Inject
  public BuildLauncher(BuildStateService buildStateService,
                       BuildService buildService,
                       AsyncHttpClient asyncHttpClient,
                       Map<String, GitHub> gitHubByHost,
                       EventBus eventBus) {
    this.buildStateService = buildStateService;
    this.buildService = buildService;
    this.asyncHttpClient = asyncHttpClient;
    this.gitHubByHost = gitHubByHost;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(Build build) throws Exception {
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

  private synchronized void startBuild(BuildDefinition buildDefinition, Build queued) throws Exception {
    String sha = currentSha(buildDefinition.getGitInfo());
    Build launching = queued.withStartTimestamp(System.currentTimeMillis()).withState(State.LAUNCHING).withSha(sha);

    buildService.begin(launching);
    LOG.info("About to launch build {}", launching.getId().get());
    HttpResponse response = asyncHttpClient.execute(buildRequest(launching)).get();
    LOG.info("Launch returned {}: {}", response.getStatusCode(), response.getAsString());
  }

  private HttpRequest buildRequest(Build build) {
    String host = System.getenv("SINGULARITY_HOST");
    String url = String.format("http://%s/singularity/v2/api/requests/request/blazar-executor/run", host);
    List<String> body = Arrays.asList("blazar-executor", "--build_id", String.valueOf(build.getId().get()));

    return HttpRequest.newBuilder()
        .setMethod(Method.POST)
        .addHeader("X-HubSpot-User", "jhaber")
        .setUrl(url)
        .setBody(body)
        .build();
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
