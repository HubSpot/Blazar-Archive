package com.hubspot.blazar.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.util.GitHubHelper;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
  public Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> blazarConfigs = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isBlazarConfig(entry.getPath())) {
        blazarConfigs.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (String blazarConfig : blazarConfigs) {
      final BuildConfig buildConfig;
      try {
        buildConfig = gitHubHelper.configFor(blazarConfig, repository, gitInfo).get();
      } catch (JsonProcessingException e) {
        LOG.warn("Error parsing config at path {} for repository {}@{}", blazarConfig, gitInfo.getFullRepositoryName(), gitInfo.getBranch());
        continue;
      }
      if (canBuild(buildConfig)) {
        String moduleName = moduleName(gitInfo, blazarConfig);
        String glob = (blazarConfig.contains("/") ? blazarConfig.substring(0, blazarConfig.lastIndexOf('/') + 1) : "") + "**";
        modules.add(new DiscoveredModule(moduleName, "config", blazarConfig, glob, buildConfig.getBuildpack(), DependencyInfo.unknown()));
      }
    }
    return modules;
  }

  private boolean canBuild(BuildConfig buildConfig) {
    return (buildConfig.getCmds().size() > 0 || buildConfig.getBuildpack().isPresent());
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
