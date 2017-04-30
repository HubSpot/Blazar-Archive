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
      if (isBuildConfig(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> buildConfigFilePaths = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isBuildConfig(entry.getPath())) {
        buildConfigFilePaths.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    Set<MalformedFile> malformedFiles = new HashSet<>();

    for (String buildConfigFilePath : buildConfigFilePaths) {
      final BuildConfig buildConfig;
      try {
        buildConfig = gitHubHelper.configFor(buildConfigFilePath, repository, gitInfo).get();
      } catch (JsonProcessingException e) {
        LOG.warn("Error parsing config at path {} for repository {}@{}", buildConfigFilePath, gitInfo.getFullRepositoryName(), gitInfo.getBranch());
        malformedFiles.add(new MalformedFile(gitInfo.getId().get(), "config", buildConfigFilePath, Throwables.getStackTraceAsString(e)));
        continue;
      }

      if (buildConfig.isDisabled()) {
        modules.add(new DiscoveredModule(
            Optional.<Integer>absent(),
            "disabled",
            "config",
            buildConfigFilePath,
            "",
            false,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            Optional.<GitInfo>absent(),
            DependencyInfo.unknown()
        ));
        continue;
      }

      if (canBuild(buildConfig)) {
        String moduleName = moduleName(gitInfo, buildConfigFilePath);
        String glob = (buildConfigFilePath.contains("/") ? buildConfigFilePath.substring(0, buildConfigFilePath.lastIndexOf('/') + 1) : "") + "**";
        modules.add(new DiscoveredModule(moduleName, "config", buildConfigFilePath, glob, buildConfig.getBuildpack(), new DependencyInfo(buildConfig.getDepends(), buildConfig.getProvides())));
      }
    }

    return new DiscoveryResult(modules, malformedFiles);
  }

  // We always want changes to blazar configs to be picked up.
  @Override
  public boolean isEnabled(GitInfo gitInfo) {
    return true;
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

  private static boolean isBuildConfig(String path) {
    return ".blazar.yaml".equals(path) || path.endsWith("/.blazar.yaml");
  }
}
