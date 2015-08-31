package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Optional;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;

@Singleton
public class BuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final BuildStateService buildStateService;
  private final BuildService buildService;
  private final AsyncHttpClient asyncHttpClient;
  private final Map<String, GitHub> gitHubByHost;
  private final ObjectMapper objectMapper;
  private final YAMLFactory yamlFactory;

  @Inject
  public BuildLauncher(BuildStateService buildStateService,
                       BuildService buildService,
                       AsyncHttpClient asyncHttpClient,
                       Map<String, GitHub> gitHubByHost,
                       EventBus eventBus,
                       ObjectMapper objectMapper,
                       YAMLFactory yamlFactory) {
    this.buildStateService = buildStateService;
    this.buildService = buildService;
    this.asyncHttpClient = asyncHttpClient;
    this.gitHubByHost = gitHubByHost;
    this.objectMapper = objectMapper;
    this.yamlFactory = yamlFactory;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(Build build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    final BuildDefinition buildDefinition;
    final Build buildToLaunch;
    if (build.getState() == State.QUEUED) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId());
      if (!buildState.getInProgressBuild().isPresent()) {
        LOG.info("No in progress build for module {}, going to launch build {}", build.getModuleId(), build.getId().get());
        buildDefinition = buildState;
        buildToLaunch = build;
      } else {
        LOG.info("In progress build for module {}, not launching build {}", build.getModuleId(), build.getId().get());
        return;
      }
    } else if (build.getState().isComplete()) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId());
      if (buildState.getPendingBuild().isPresent()) {
        LOG.info("Pending build for module {}, going to launch build {}", build.getModuleId(), buildState.getPendingBuild().get().getId().get());
        buildDefinition = buildState;
        buildToLaunch = buildState.getPendingBuild().get();
      } else {
        LOG.info("No pending build for module {}", build.getModuleId());
        return;
      }
    } else {
      return;
    }

    startBuild(buildDefinition, buildToLaunch);
  }

  private synchronized void startBuild(BuildDefinition definition, Build queued) throws Exception {
    Optional<String> sha = currentSha(definition.getGitInfo());
    if (sha.isPresent()) {
      BuildConfig buildConfig = configAtSha(definition, sha.get());
      Build launching = queued.withStartTimestamp(System.currentTimeMillis()).withState(State.LAUNCHING).withSha(sha.get()).withBuildConfig(buildConfig);

      LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
      buildService.begin(launching);
      LOG.info("About to launch build {}", launching.getId().get());
      HttpResponse response = asyncHttpClient.execute(buildRequest(definition.getModule(), launching)).get();
      LOG.info("Launch returned {}: {}", response.getStatusCode(), response.getAsString());
    } else {
      LOG.info("Failing build {}", queued.getId().get());
      // TODO
      Build failed = queued.withState(State.FAILED).withLog("https://fake.com").withEndTimestamp(System.currentTimeMillis());
      buildService.update(failed);
    }
  }

  private HttpRequest buildRequest(Module module, Build build) {
    String host = System.getenv("SINGULARITY_HOST");
    String url = String.format("http://%s/singularity/v2/api/requests/request/blazar-executor/run", host);

    String buildId = String.valueOf(build.getId().get());
    List<String> body = Arrays.asList("blazar-executor", "--build_id", buildId, buildCommand(module));

    return HttpRequest.newBuilder()
        .setMethod(Method.POST)
        .addHeader("X-HubSpot-User", "jhaber")
        .setUrl(url)
        .setBody(body)
        .build();
  }

  private String buildCommand(Module module) {
    if (module.getPath().endsWith(".blazar.yaml")) {
      return "--safe_mode";
    }

    String whitelist = Objects.firstNonNull(System.getenv("BUILD_WHITELIST"), "");

    List<String> modulesToBuild = Splitter.on(',').omitEmptyStrings().splitToList(whitelist);
    return modulesToBuild.contains(module.getName()) ? "--safe_mode" : "--dry_run";
  }

  private BuildConfig configAtSha(BuildDefinition definition, String sha) throws IOException {
    GitHub gitHub = gitHubFor(definition.getGitInfo());
    GHRepository repository = gitHub.getRepository(definition.getGitInfo().getFullRepositoryName());

    String modulePath = definition.getModule().getPath();
    String moduleFolder = modulePath.contains("/") ? modulePath.substring(0, modulePath.lastIndexOf('/') + 1) : "";
    String configPath = moduleFolder + ".blazar.yaml";

    String repositoryName = definition.getGitInfo().getFullRepositoryName();
    LOG.info("Going to fetch config for path {} in repo {}@{}", configPath, repositoryName, sha);

    try {
      GHContent configContent = repository.getFileContent(configPath, sha);
      LOG.info("Found config for path {} in repo {}@{}", configPath, repositoryName, sha);
      return objectMapper.readValue(yamlFactory.createParser(configContent.getContent()), BuildConfig.class);
    } catch (FileNotFoundException e) {
      LOG.info("No config found for path {} in repo {}@{}, using default values", configPath, repositoryName, sha);
      return BuildConfig.makeDefaultBuildConfig();
    }
  }

  private Optional<String> currentSha(GitInfo gitInfo) throws IOException {
    LOG.info("Trying to fetch current sha for branch {}/{}", gitInfo.getRepository(), gitInfo.getBranch());
    GitHub gitHub = gitHubFor(gitInfo);

    GHRepository repository = gitHub.getRepository(gitInfo.getFullRepositoryName());
    GHBranch branch = repository.getBranches().get(gitInfo.getBranch());
    if (branch == null) {
      LOG.info("Couldn't find branch {} for repository {}", gitInfo.getBranch(), gitInfo.getFullRepositoryName());
      return Optional.absent();
    } else {
      LOG.info("Found sha {} for branch {}/{}", branch.getSHA1(), gitInfo.getRepository(), gitInfo.getBranch());
      return Optional.of(branch.getSHA1());
    }
  }

  private GitHub gitHubFor(GitInfo gitInfo) {
    String host = gitInfo.getHost();

    return Preconditions.checkNotNull(gitHubByHost.get(host), "No GitHub found for host " + host);
  }
}
