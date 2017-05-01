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
import com.google.common.base.Throwables;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildConfigDiscoveryResult;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveredBuildConfig;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class BuildConfigDiscovery {
  private static final Logger LOG = LoggerFactory.getLogger(BuildConfigDiscovery.class);

  private final GitHubHelper gitHubHelper;

  @Inject
  public BuildConfigDiscovery(GitHubHelper gitHubHelper) {
    this.gitHubHelper = gitHubHelper;
  }

  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (String path : gitHubHelper.affectedPaths(commitInfo)) {
      if (isBuildConfig(path)) {
        return true;
      }
    }

    return false;
  }

  public BuildConfigDiscoveryResult discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> buildConfigFilePaths = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isBuildConfig(entry.getPath())) {
        buildConfigFilePaths.add(entry.getPath());
      }
    }

    Set<DiscoveredBuildConfig> discoveredBuildConfigs = new HashSet<>();
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

      String glob = (buildConfigFilePath.contains("/") ?
          buildConfigFilePath.substring(0, buildConfigFilePath.lastIndexOf('/') + 1) : "") + "**";

      discoveredBuildConfigs.add(new DiscoveredBuildConfig(buildConfig, buildConfigFilePath, glob));
    }

    return new BuildConfigDiscoveryResult(discoveredBuildConfigs, malformedFiles);
  }

  // We always want changes to blazar configs to be picked up.
  public boolean isEnabled(GitInfo gitInfo) {
    return true;
  }

  private static boolean isBuildConfig(String path) {
    return ".blazar.yaml".equals(path) || path.endsWith("/.blazar.yaml");
  }
}
