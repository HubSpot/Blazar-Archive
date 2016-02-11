package com.hubspot.blazar.discovery.hubspotstatic;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  private static final Optional<GitInfo> BUILD_CONFIGURATION = Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Static#master"));
  private static final String STATIC_CONF = "static_conf.json";

  private final GitHubHelper gitHubHelper;
  private final ObjectMapper objectMapper;

  @Inject
  public HubSpotStaticModuleDiscovery(GitHubHelper gitHubHelper, ObjectMapper objectMapper) {
    this.gitHubHelper = gitHubHelper;
    this.objectMapper = objectMapper;
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
    for (String config: staticConfigs) {
      String moduleName = moduleName(gitInfo, config);
      String glob = (config.contains("/") ? config.substring(0, config.lastIndexOf('/') + 1) : "") + "**";
      modules.add(new DiscoveredModule(moduleName, "hubspotStatic", config, glob,  BUILD_CONFIGURATION, getDockerfileDeps()));
    }
    return new DiscoveryResult(modules, Collections.<MalformedFile>emptySet());
  }

  private DependencyInfo getDockerfileDeps() throws IOException {
    // todo no dep discovery
    Set<String> emtpySet = Collections.emptySet();
    return new DependencyInfo(emtpySet, emtpySet);
  }

  private String moduleName(GitInfo gitInfo, String path) throws IOException {
    String contents = gitHubHelper.contentsFor(path, gitHubHelper.repositoryFor(gitInfo), gitInfo);
    return objectMapper.readValue(contents, HubSpotStaticConf.class).getName();
  }

  private static String folderName(String path) {
    String folderPath = path.substring(0, path.lastIndexOf('/'));
    return folderPath.contains("/") ? folderPath.substring(folderPath.lastIndexOf('/') + 1) : folderPath;
  }

  private static boolean isStaticConfig(String path) {
    return STATIC_CONF.equals(path) || path.endsWith("/"+STATIC_CONF);
  }
}

