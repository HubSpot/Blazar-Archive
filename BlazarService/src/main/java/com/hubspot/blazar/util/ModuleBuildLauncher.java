package com.hubspot.blazar.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.horizon.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class ModuleBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final SingularityBuildLauncher singularityBuildLauncher;
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final BranchService branchService;
  private final ModuleService moduleService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public ModuleBuildLauncher(SingularityBuildLauncher singularityBuildLauncher,
                             RepositoryBuildService repositoryBuildService,
                             ModuleBuildService moduleBuildService,
                             BranchService branchService,
                             ModuleService moduleService,
                             GitHubHelper gitHubHelper,
                             EventBus eventBus) {
    this.singularityBuildLauncher = singularityBuildLauncher;
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.gitHubHelper = gitHubHelper;

    eventBus.register(this);
  }

  @Subscribe
  public void handleRepositoryBuild(RepositoryBuild build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    if (build.getState() == RepositoryBuild.State.LAUNCHING) {
      Set<Module> modules = moduleService.getByBranch(build.getBranchId());
      Set<Module> toBuild = findModulesToBuild(build.getCommitInfo().get(), modules);

      for (Module module : toBuild) {
        moduleBuildService.enqueue(build, module);
      }
    }
  }

  @Subscribe
  public void handleModuleBuild(ModuleBuild build) throws Exception {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    switch (build.getState()) {
      case QUEUED:
        if (dependencyGraph.upstreamVertices(build.getModuleId()).isEmpty()) {
          launchBuild(repositoryBuild, build);
        }
        break;
      case SUCCEEDED:
        Set<Integer> downstreamModules = dependencyGraph.reachableVertices(build.getModuleId());
        if (downstreamModules.isEmpty()) {
          // check if we're the last module, complete repository build if so (maybe move to RepositoryBuildLauncher?)
        } else {
          for (int downstreamModule : dependencyGraph.reachableVertices(build.getModuleId())) {
            // launch build if all upstreams are done and still queued
          }
        }
        break;
      case CANCELLED:
      case FAILED:
        // cancel all downstream modules
        for (int downstreamModule : dependencyGraph.reachableVertices(build.getModuleId())) {
          // cancel build if queued
        }
        break;
    }
  }

  private void launchBuild(RepositoryBuild repositoryBuild, ModuleBuild build) throws Exception {
    GitInfo gitInfo = branchService.get(repositoryBuild.getBranchId()).get().withBranch(repositoryBuild.getSha().get());
    Module module = moduleService.get(build.getModuleId()).get();

    BuildConfig buildConfig = configAtSha(gitInfo, module);
    BuildConfig resolvedConfig = resolveConfig(buildConfig, module);

    ModuleBuild launching = build.withStartTimestamp(System.currentTimeMillis())
        .withState(State.LAUNCHING)
        .withBuildConfig(buildConfig)
        .withResolvedConfig(resolvedConfig);

    LOG.info("Updating status of build {} to {}", launching.getId().get(), launching.getState());
    moduleBuildService.begin(launching);
    LOG.info("About to launch build {}", launching.getId().get());
    HttpResponse response = singularityBuildLauncher.launchBuild(launching);
    LOG.info("Launch returned {}: {}", response.getStatusCode(), response.getAsString());
  }

  private BuildConfig resolveConfig(BuildConfig buildConfig, Module module) throws IOException, NonRetryableBuildException {
    if (buildConfig.getBuildpack().isPresent()) {
      BuildConfig buildpackConfig = fetchBuildpack(buildConfig.getBuildpack().get());
      return mergeConfig(buildConfig, buildpackConfig);
    } else if (module.getBuildpack().isPresent()) {
      BuildConfig buildpackConfig = fetchBuildpack(module.getBuildpack().get());
      return mergeConfig(buildConfig, buildpackConfig);
    } else {
      return buildConfig;
    }
  }

  private BuildConfig fetchBuildpack(GitInfo gitInfo) throws IOException, NonRetryableBuildException {
    return configAtSha(gitInfo, ".blazar-buildpack.yaml");
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

  private BuildConfig configAtSha(GitInfo gitInfo, Module module) throws IOException, NonRetryableBuildException {
    String folder = module.getFolder();
    String configPath = folder + (folder.isEmpty() ? "" : "/") + ".blazar.yaml";

    return configAtSha(gitInfo, configPath);
  }

  private BuildConfig configAtSha(GitInfo gitInfo, String configPath) throws IOException, NonRetryableBuildException {
    String repositoryName = gitInfo.getFullRepositoryName();
    LOG.info("Going to fetch config for path {} in repo {}@{}", configPath, repositoryName, gitInfo.getBranch());

    try {
      Optional<BuildConfig> buildConfig = gitHubHelper.configFor(configPath, gitInfo);
      return buildConfig.or(BuildConfig.makeDefaultBuildConfig());
    } catch (JsonProcessingException e) {
      String message = String.format("Invalid config found for path %s in repo %s@%s, failing build", configPath, repositoryName, gitInfo.getBranch());
      throw new NonRetryableBuildException(message, e);
    }
  }

  private Set<Module> findModulesToBuild(CommitInfo commitInfo, Set<Module> modules) {
    final Set<Module> toBuild = new HashSet<>();
    if (commitInfo.isTruncated()) {
      toBuild.addAll(modules);
    } else {
      for (String path : gitHubHelper.affectedPaths(commitInfo)) {
        for (Module module : modules) {
          if (module.contains(FileSystems.getDefault().getPath(path))) {
            toBuild.add(module);
          }
        }
      }
    }

    return toBuild;
  }
}
