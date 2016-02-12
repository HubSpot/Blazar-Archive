package com.hubspot.blazar.discovery.hsstatic;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class HubSpotStaticModuleDiscovery implements ModuleDiscovery {
  private static final Optional<GitInfo> BUILD_CONFIGURATION = Optional.of(GitInfo.fromString("git.hubteam.com/HubSpot/Blazar-Buildpack-Static#master"));
  private static final String STATIC_CONF = "static/static_conf.json";

  private final GitHubHelper gitHubHelper;

  @Inject
  public HubSpotStaticModuleDiscovery(GitHubHelper gitHubHelper) {
    this.gitHubHelper = gitHubHelper;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (String path : gitHubHelper.affectedPaths(commitInfo)) {
      if (isStaticConfig(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> staticConfigs = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isStaticConfig(entry.getPath())) {
        staticConfigs.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    Set<MalformedFile> malformedFiles = new HashSet<>();
    for (String config : staticConfigs) {
      String moduleDirectory = String.valueOf(Paths.get(config).getParent());
      String moduleName = moduleName(gitInfo, moduleDirectory);
      String glob = (moduleDirectory.contains("/") ? moduleDirectory.substring(0, moduleDirectory.lastIndexOf('/') + 1) : "") + "**";
      modules.add(new DiscoveredModule(moduleName, "hs-static", moduleDirectory, glob, BUILD_CONFIGURATION, DependencyInfo.unknown()));
    }
    return new DiscoveryResult(modules, malformedFiles);
  }

  private static String moduleName(GitInfo gitInfo, String path) {
    return path.contains("/") ? folderName(path) : gitInfo.getRepository();
  }

  private static String folderName(String path) {
    String folderPath = path.substring(0, path.lastIndexOf('/'));
    return folderPath.contains("/") ? folderPath.substring(folderPath.lastIndexOf('/') + 1) : folderPath;
  }

  private static boolean isStaticConfig(String path) {
    return STATIC_CONF.equals(path) || path.endsWith("/" + STATIC_CONF);
  }
}
