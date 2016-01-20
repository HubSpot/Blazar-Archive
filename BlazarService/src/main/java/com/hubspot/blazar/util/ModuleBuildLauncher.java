package com.hubspot.blazar.util;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildStep;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.exception.NonRetryableBuildException;

@Singleton
public class ModuleBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleBuildLauncher.class);

  private final ModuleBuildService moduleBuildService;
  private final BranchService branchService;
  private final ModuleService moduleService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public ModuleBuildLauncher(ModuleBuildService moduleBuildService,
                             BranchService branchService,
                             ModuleService moduleService,
                             GitHubHelper gitHubHelper) {
    this.moduleBuildService = moduleBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.gitHubHelper = gitHubHelper;
  }

  public void launch(RepositoryBuild repositoryBuild, ModuleBuild build) throws Exception {
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
    List<BuildStep> steps = primary.getSteps().isEmpty() ? secondary.getSteps() : primary.getSteps();
    Map<String, String> env = new LinkedHashMap<>();
    env.putAll(secondary.getEnv());
    env.putAll(primary.getEnv());
    List<String> buildDeps = Lists.newArrayList(Iterables.concat(secondary.getBuildDeps(), primary.getBuildDeps()));
    List<String> webhooks = Lists.newArrayList(Iterables.concat(secondary.getWebhooks(), primary.getWebhooks()));
    List<String> cache = Lists.newArrayList(Iterables.concat(secondary.getCache(), primary.getCache()));

    return new BuildConfig(steps, env, buildDeps, webhooks, cache, Optional.<GitInfo>absent());
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
}
