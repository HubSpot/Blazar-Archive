package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.User;
import com.hubspot.singularity.SingularityClientCredentials;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitUser;
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
  private final SingularityConfiguration singularityConfiguration;
  private final Map<String, GitHub> gitHubByHost;
  private final ObjectMapper objectMapper;
  private final YAMLFactory yamlFactory;

  @Inject
  public BuildLauncher(BuildStateService buildStateService,
                       BuildService buildService,
                       AsyncHttpClient asyncHttpClient,
                       BlazarConfiguration configuration,
                       Map<String, GitHub> gitHubByHost,
                       EventBus eventBus,
                       ObjectMapper objectMapper,
                       YAMLFactory yamlFactory) {
    this.buildStateService = buildStateService;
    this.buildService = buildService;
    this.asyncHttpClient = asyncHttpClient;
    this.singularityConfiguration = configuration.getSingularityConfiguration();
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
    final Optional<Build> previousBuild;
    if (build.getState() == State.QUEUED) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId()).get();
      Optional<Build> pendingBuild = buildState.getPendingBuild();
      if (buildState.getInProgressBuild().isPresent()) {
        LOG.info("In progress build for module {}, not launching build {}", build.getModuleId(), build.getId().get());
        return;
      } else if (!pendingBuild.isPresent() || !build.getId().equals(pendingBuild.get().getId())) {
        LOG.info("Build {} is no longer pending for module {}, not launching", build.getId().get(), build.getModuleId());
        return;
      } else {
        LOG.info("No in progress build for module {}, going to launch build {}", build.getModuleId(), build.getId().get());
        buildDefinition = buildState;
        buildToLaunch = pendingBuild.get();
        previousBuild = buildState.getLastBuild();
      }
    } else if (build.getState().isComplete()) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId()).get();
      if (buildState.getPendingBuild().isPresent()) {
        LOG.info("Pending build for module {}, going to launch build {}", build.getModuleId(), buildState.getPendingBuild().get().getId().get());
        buildDefinition = buildState;
        buildToLaunch = buildState.getPendingBuild().get();
        previousBuild = buildState.getLastBuild();
      } else {
        LOG.info("No pending build for module {}", build.getModuleId());
        return;
      }
    } else {
      return;
    }

    try {
      startBuild(buildDefinition, buildToLaunch, previousBuild);
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", buildToLaunch.getId().get(), e);
      buildService.fail(buildToLaunch);
    }
  }

  private synchronized void startBuild(BuildDefinition definition, Build queued, Optional<Build> previous) throws Exception {
    CommitInfo commitInfo = commitInfo(definition.getGitInfo(), commit(previous));
    BuildConfig buildConfig = configAtSha(definition, commitInfo.getCurrent().getId());
    BuildConfig resolvedConfig = resolveConfig(buildConfig, definition);

    Build launching = queued.withStartTimestamp(System.currentTimeMillis())
        .withState(State.LAUNCHING)
        .withCommitInfo(commitInfo)
        .withBuildConfig(buildConfig)
        .withResolvedConfig(resolvedConfig);

    LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
    buildService.begin(launching);
    LOG.info("About to launch build {}", launching.getId().get());
    HttpResponse response = asyncHttpClient.execute(buildRequest(definition, launching)).get();
    LOG.info("Launch returned {}: {}", response.getStatusCode(), response.getAsString());
  }

  private BuildConfig resolveConfig(BuildConfig buildConfig, BuildDefinition definition) throws IOException, NonRetryableBuildException {
    if (buildConfig.getBuildpack().isPresent()) {
      BuildConfig buildpackConfig = fetchBuildpack(buildConfig.getBuildpack().get());
      return mergeConfig(buildConfig, buildpackConfig);
    } else if (definition.getModule().getBuildpack().isPresent()) {
      BuildConfig buildpackConfig = fetchBuildpack(definition.getModule().getBuildpack().get());
      return mergeConfig(buildConfig, buildpackConfig);
    } else {
      return buildConfig;
    }
  }

  private BuildConfig fetchBuildpack(GitInfo gitInfo) throws IOException, NonRetryableBuildException {
    return configAtSha(gitInfo, ".blazar-buildpack.yaml", gitInfo.getBranch());
  }

  private static BuildConfig mergeConfig(BuildConfig primary, BuildConfig secondary) {
    List<String> cmds = primary.getCmds().isEmpty() ? secondary.getCmds() : primary.getCmds();
    Map<String, String> env = new LinkedHashMap<>();
    env.putAll(secondary.getEnv());
    env.putAll(primary.getEnv());
    List<String> buildDeps = Lists.newArrayList(Iterables.concat(secondary.getBuildDeps(), primary.getBuildDeps()));
    List<String> webhooks = Lists.newArrayList(Iterables.concat(secondary.getWebhooks(), primary.getWebhooks()));
    List<String> cache = Lists.newArrayList(Iterables.concat(secondary.getCache(), primary.getCache()));

    return new BuildConfig(cmds, env, buildDeps, webhooks, cache, Optional.<GitInfo>absent());
  }

  private HttpRequest buildRequest(BuildDefinition definition, Build build) {
    String buildId = String.valueOf(build.getId().get());
    List<String> body = Arrays.asList("blazar-executor", "--build_id", buildId, "--blazar_api_url", "http://bootstrap.hubteam.com/blazar/v1", buildCommand(definition, build));

    return buildRequest(body);
  }

  private HttpRequest buildRequest(Object body) {
    Optional<SingularityClientCredentials> credentials = singularityConfiguration.getCredentials();

    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .setMethod(Method.POST)
        .setUrl(buildUrl())
        .setBody(body);

    if (credentials.isPresent()) {
      builder.addHeader(credentials.get().getHeaderName(), credentials.get().getToken());
    }

    return builder.build();
  }

  private String buildUrl() {
    String host = singularityConfiguration.getHost();
    String path = singularityConfiguration.getPath().or("singularity/api");
    return String.format("http://%s/%s/requests/request/blazar-executor/run", host, path);
  }

  private String buildCommand(BuildDefinition definition, Build build) {
    BuildConfig buildConfig = build.getBuildConfig().get();
    if (!buildConfig.getCmds().isEmpty() || buildConfig.getBuildpack().isPresent()) {
      return "--safe_mode";
    }

    String whitelist = Objects.firstNonNull(System.getenv("BUILD_WHITELIST"), "");

    List<String> reposToBuild = Splitter.on(',').omitEmptyStrings().splitToList(whitelist);
    return reposToBuild.contains(definition.getGitInfo().getRepository()) ? "--safe_mode" : "--dry_run";
  }

  private BuildConfig configAtSha(BuildDefinition definition, String sha) throws IOException, NonRetryableBuildException {
    String modulePath = definition.getModule().getPath();
    String moduleFolder = modulePath.contains("/") ? modulePath.substring(0, modulePath.lastIndexOf('/') + 1) : "";
    String configPath = moduleFolder + ".blazar.yaml";

    return configAtSha(definition.getGitInfo(), configPath, sha);
  }

  private BuildConfig configAtSha(GitInfo gitInfo, String configPath, String sha) throws IOException, NonRetryableBuildException {
    GitHub gitHub = gitHubFor(gitInfo);
    GHRepository repository = gitHub.getRepository(gitInfo.getFullRepositoryName());

    String repositoryName = gitInfo.getFullRepositoryName();
    LOG.info("Going to fetch config for path {} in repo {}@{}", configPath, repositoryName, sha);

    try {
      GHContent configContent = repository.getFileContent(configPath, sha);
      LOG.info("Found config for path {} in repo {}@{}", configPath, repositoryName, sha);
      JsonParser parser = yamlFactory.createParser(configContent.getContent());
      return objectMapper.readValue(parser, BuildConfig.class);
    } catch (FileNotFoundException e) {
      LOG.info("No config found for path {} in repo {}@{}, using default values", configPath, repositoryName, sha);
      return BuildConfig.makeDefaultBuildConfig();
    } catch (JsonProcessingException e) {
      String message = String.format("Invalid config found for path %s in repo %s@%s, failing build", configPath, repositoryName, sha);
      throw new NonRetryableBuildException(message, e);
    }
  }

  private CommitInfo commitInfo(GitInfo gitInfo, Optional<Commit> previousCommit) throws IOException, NonRetryableBuildException {
    LOG.info("Trying to fetch current sha for branch {}/{}", gitInfo.getRepository(), gitInfo.getBranch());
    GitHub gitHub = gitHubFor(gitInfo);

    final GHRepository repository;
    try {
      repository = gitHub.getRepository(gitInfo.getFullRepositoryName());
    } catch (FileNotFoundException e) {
      throw new NonRetryableBuildException("Couldn't find repository " + gitInfo.getFullRepositoryName(), e);
    }

    GHBranch branch = repository.getBranches().get(gitInfo.getBranch());
    if (branch == null) {
      String message = String.format("Couldn't find branch %s for repository %s", gitInfo.getBranch(), gitInfo.getFullRepositoryName());
      throw new NonRetryableBuildException(message);
    } else {
      LOG.info("Found sha {} for branch {}/{}", branch.getSHA1(), gitInfo.getRepository(), gitInfo.getBranch());

      Commit commit = toCommit(repository.getCommit(branch.getSHA1()));
      final List<Commit> newCommits;
      boolean truncated = false;
      if (previousCommit.isPresent()) {
        newCommits = new ArrayList<>();

        List<GHCompare.Commit> commits = Collections.emptyList();
        try {
          GHCompare compare = repository.getCompare(previousCommit.get().getId(), commit.getId());
          commits = Arrays.asList(compare.getCommits());
        } catch (FileNotFoundException e) {
          LOG.warn("Error generating compare from sha {} to sha {}", previousCommit.get().getId(), commit.getId(), e);
        }

        if (commits.size() > 10) {
          commits = commits.subList(commits.size() - 10, commits.size());
          truncated = true;
        }

        for (GHCompare.Commit newCommit : commits) {
          newCommits.add(toCommit(repository.getCommit(newCommit.getSHA1())));
        }
      } else {
        newCommits = Collections.emptyList();
      }

      return new CommitInfo(commit, previousCommit, newCommits, truncated);
    }
  }

  private GitHub gitHubFor(GitInfo gitInfo) {
    String host = gitInfo.getHost();

    return Preconditions.checkNotNull(gitHubByHost.get(host), "No GitHub found for host " + host);
  }

  private static Commit toCommit(GHCommit commit) throws IOException {
    Commit.Builder builder = Commit.newBuilder()
        .setId(commit.getSHA1())
        .setMessage(commit.getCommitShortInfo().getMessage())
        .setTimestamp(String.valueOf(commit.getCommitShortInfo().getCommitter().getDate().getTime()))
        .setUrl(commit.getOwner().getHtmlUrl() + "/commit/" + commit.getSHA1())
        .setAuthor(toAuthor(commit))
        .setCommitter(toCommitter(commit));

    for (GHCommit.File file : commit.getFiles()) {
      switch (file.getStatus()) {
        case "added":
          builder.addAdded(file.getFileName());
          break;
        case "modified":
        case "changed":
        case "renamed":
          builder.addModified(file.getFileName());
          break;
        case "removed":
          builder.addRemoved(file.getFileName());
          break;
        default:
          throw new IllegalArgumentException("Unrecognized file status: " + file.getStatus());
      }
    }

    return builder.build();
  }

  private static User toAuthor(GHCommit commit) throws IOException {
    User.Builder builder = toUser(commit.getCommitShortInfo().getAuthor());

    if (commit.getAuthor() != null && commit.getAuthor().getLogin() != null) {
      builder.setUsername(commit.getAuthor().getLogin());
    }

    return builder.build();
  }

  private static User toCommitter(GHCommit commit) throws IOException {
    User.Builder builder = toUser(commit.getCommitShortInfo().getCommitter());

    if (commit.getCommitter() != null && commit.getCommitter().getLogin() != null) {
      builder.setUsername(commit.getCommitter().getLogin());
    }

    return builder.build();
  }

  private static User.Builder toUser(GitUser user) {
    return User.newBuilder().setName(user.getName()).setEmail(user.getEmail());

  }

  private static Optional<Commit> commit(Optional<Build> build) {
    if (build.isPresent() && build.get().getCommitInfo().isPresent()) {
      return Optional.of(build.get().getCommitInfo().get().getCurrent());
    } else {
      return Optional.absent();
    }
  }
}
