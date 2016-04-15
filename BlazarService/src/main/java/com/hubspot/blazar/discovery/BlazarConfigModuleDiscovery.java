package com.hubspot.blazar.discovery;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class BlazarConfigModuleDiscovery implements ModuleDiscovery {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarConfigModuleDiscovery.class);

  private final GitHubHelper gitHubHelper;

  @Inject
  public BlazarConfigModuleDiscovery(GitHubHelper gitHubHelper) {
    this.gitHubHelper = gitHubHelper;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (String path : gitHubHelper.affectedPaths(commitInfo)) {
      if (isBlazarConfig(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> blazarConfigs = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isBlazarConfig(entry.getPath())) {
        blazarConfigs.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    Set<MalformedFile> malformedFiles = new HashSet<>();
    for (String blazarConfig : blazarConfigs) {
      if (disabled(blazarConfig, repository, gitInfo)) {
        modules.add(new DiscoveredModule(
            Optional.<Integer>absent(),
            "disabled",
            "config",
            blazarConfig,
            "",
            false,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            Optional.<GitInfo>absent(),
            DependencyInfo.unknown()
        ));
        continue;
      }

      final BuildConfig buildConfig;
      try {
        buildConfig = gitHubHelper.configFor(blazarConfig, repository, gitInfo).get();
      } catch (JsonProcessingException e) {
        LOG.warn("Error parsing config at path {} for repository {}@{}", blazarConfig, gitInfo.getFullRepositoryName(), gitInfo.getBranch());
        malformedFiles.add(new MalformedFile(gitInfo.getId().get(), "config", blazarConfig, Throwables.getStackTraceAsString(e)));
        continue;
      }

      if (canBuild(buildConfig)) {
        String moduleName = moduleName(gitInfo, blazarConfig);
        String glob = (blazarConfig.contains("/") ? blazarConfig.substring(0, blazarConfig.lastIndexOf('/') + 1) : "") + "**";
        modules.add(new DiscoveredModule(moduleName, "config", blazarConfig, glob, buildConfig.getBuildpack(), new DependencyInfo(buildConfig.getDepends(), buildConfig.getProvides())));
      }
    }

    return new DiscoveryResult(modules, malformedFiles);
  }

  private boolean disabled(String blazarConfig, GHRepository repository, GitInfo gitInfo) throws IOException {
    return gitHubHelper.contentsFor(blazarConfig, repository, gitInfo).contains("enabled: false");
  }

  private boolean canBuild(BuildConfig buildConfig) {
    return (buildConfig.getSteps().size() > 0 || buildConfig.getBuildpack().isPresent());
  }

  private static String moduleName(GitInfo gitInfo, String path) {
    return path.contains("/") ? folderName(path) : gitInfo.getRepository();
  }

  private static String folderName(String path) {
    String folderPath = path.substring(0, path.lastIndexOf('/'));
    return folderPath.contains("/") ? folderPath.substring(folderPath.lastIndexOf('/') + 1) : folderPath;
  }

  private static boolean isBlazarConfig(String path) {
    return ".blazar.yaml".equals(path) || path.endsWith("/.blazar.yaml");
  }
}
