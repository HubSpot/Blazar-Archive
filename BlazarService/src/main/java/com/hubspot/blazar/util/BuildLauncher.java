package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.User;
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
    final Optional<Build> previousBuild;
    if (build.getState() == State.QUEUED) {
      BuildState buildState = buildStateService.getByModule(build.getModuleId());
      if (!buildState.getInProgressBuild().isPresent()) {
        LOG.info("No in progress build for module {}, going to launch build {}", build.getModuleId(), build.getId().get());
        buildDefinition = buildState;
        buildToLaunch = build;
        previousBuild = buildState.getLastBuild();
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
        previousBuild = buildState.getLastBuild();
      } else {
        LOG.info("No pending build for module {}", build.getModuleId());
        return;
      }
    } else {
      return;
    }

    startBuild(buildDefinition, buildToLaunch, previousBuild);
  }

  private synchronized void startBuild(BuildDefinition definition, Build queued, Optional<Build> previous) throws Exception {
    Optional<CommitInfo> commitInfo = commitInfo(definition.getGitInfo(), sha(previous));
    final Optional<BuildConfig> buildConfig;
    if (commitInfo.isPresent()) {
      buildConfig = configAtSha(definition, commitInfo.get().getCurrent().getId());
    } else {
      buildConfig = Optional.absent();
    }

    if (buildConfig.isPresent()) {
      Build launching = queued.withStartTimestamp(System.currentTimeMillis())
          .withState(State.LAUNCHING)
          .withCommitInfo(commitInfo.get())
          .withBuildConfig(buildConfig.get());

      LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
      buildService.begin(launching);
      LOG.info("About to launch build {}", launching.getId().get());
      HttpResponse response = asyncHttpClient.execute(buildRequest(definition.getModule(), launching)).get();
      LOG.info("Launch returned {}: {}", response.getStatusCode(), response.getAsString());
    } else {
      LOG.info("Failing build {}", queued.getId().get());
      buildService.begin(queued.withState(State.LAUNCHING));
      buildService.update(queued.withState(State.FAILED).withEndTimestamp(System.currentTimeMillis()));
    }
  }

  private HttpRequest buildRequest(Module module, Build build) {
    String host = System.getenv("SINGULARITY_HOST");
    String url = String.format("http://%s/singularity/v2/api/requests/request/blazar-executor/run", host);

    String buildId = String.valueOf(build.getId().get());
    List<String> body = Arrays.asList("blazar-executor", "--build_id", buildId, "--blazar_url", "http://bootstrap.hubteam.com/blazar/v1", buildCommand(module));

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

  private Optional<BuildConfig> configAtSha(BuildDefinition definition, String sha) throws IOException {
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
      JsonParser parser = yamlFactory.createParser(configContent.getContent());
      return Optional.of(objectMapper.readValue(parser, BuildConfig.class));
    } catch (FileNotFoundException e) {
      LOG.info("No config found for path {} in repo {}@{}, using default values", configPath, repositoryName, sha);
      return Optional.of(BuildConfig.makeDefaultBuildConfig());
    } catch (JsonProcessingException e) {
      LOG.info("Invalid config found for path {} in repo {}@{}, failing build", configPath, repositoryName, sha);
      return Optional.absent();
    }
  }

  private Optional<CommitInfo> commitInfo(GitInfo gitInfo, Optional<String> previousSha) throws IOException {
    LOG.info("Trying to fetch current sha for branch {}/{}", gitInfo.getRepository(), gitInfo.getBranch());
    GitHub gitHub = gitHubFor(gitInfo);

    final GHRepository repository;
    try {
      repository = gitHub.getRepository(gitInfo.getFullRepositoryName());
    } catch (FileNotFoundException e) {
      LOG.info("Couldn't find repository {}", gitInfo.getFullRepositoryName());
      return Optional.absent();
    }

    GHBranch branch = repository.getBranches().get(gitInfo.getBranch());
    if (branch == null) {
      LOG.info("Couldn't find branch {} for repository {}", gitInfo.getBranch(), gitInfo.getFullRepositoryName());
      return Optional.absent();
    } else {
      LOG.info("Found sha {} for branch {}/{}", branch.getSHA1(), gitInfo.getRepository(), gitInfo.getBranch());

      Commit commit = toCommit(repository.getCommit(branch.getSHA1()));
      final List<Commit> newCommits;
      if (previousSha.isPresent()) {
        newCommits = new ArrayList<>();

        GHCompare compare = repository.getCompare(previousSha.get(), commit.getId());
        for (GHCompare.Commit newCommit : compare.getCommits()) {
          newCommits.add(toCommit(repository.getCommit(newCommit.getSHA1())));
        }
      } else {
        newCommits = Collections.emptyList();
      }

      return Optional.of(new CommitInfo(commit, newCommits));
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

  private static Optional<String> sha(Optional<Build> build) {
    if (!build.isPresent()) {
      return Optional.absent();
    } else if (build.get().getCommitInfo() != null && build.get().getCommitInfo().isPresent()) {
      return Optional.of(build.get().getCommitInfo().get().getCurrent().getId());
    } else {
      return build.get().getSha();
    }
  }
}
