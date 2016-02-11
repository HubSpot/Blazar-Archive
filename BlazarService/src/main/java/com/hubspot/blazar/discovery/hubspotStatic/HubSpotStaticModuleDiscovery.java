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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
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
  private static final Logger LOG = LoggerFactory.getLogger(HubSpotStaticModuleDiscovery.class);

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
    Set<MalformedFile> malformedFiles = new HashSet<>();
    for (String config: staticConfigs) {
      try {
        String moduleName = moduleName(gitInfo, repository, config);
        Preconditions.checkNotNull(moduleName);
        String glob = (config.contains("/") ? config.substring(0, config.lastIndexOf('/') + 1) : "") + "**";
        modules.add(new DiscoveredModule(moduleName, "hubspotStatic", config, glob, BUILD_CONFIGURATION, getDockerfileDeps()));
      } catch (NullPointerException e) {
        LOG.error("Error module name in config {} in repo:branch {}:{} was null.", config, gitInfo.getFullRepositoryName(), gitInfo.getBranch());
        malformedFiles.add(new MalformedFile(gitInfo.getId().get(), "hubspotStatic", config, Throwables.getStackTraceAsString(e)));
      } catch (IOException e) {
        LOG.error("IO Error getting {} from GHE on repo:branch {}:{}.");
        malformedFiles.add(new MalformedFile(gitInfo.getId().get(), "hubspotStatic", config, Throwables.getStackTraceAsString(e)));
      }
    }
    return new DiscoveryResult(modules, malformedFiles);
  }

  private DependencyInfo getDockerfileDeps() throws IOException {
    // todo no dep discovery
    Set<String> emtpySet = Collections.emptySet();
    return new DependencyInfo(emtpySet, emtpySet);
  }

  private String moduleName(GitInfo gitInfo, GHRepository repository, String path) throws IOException {
    String contents = gitHubHelper.contentsFor(path, repository, gitInfo);
    return objectMapper.readValue(contents, HubSpotStaticConf.class).getName();
  }

  private static boolean isStaticConfig(String path) {
    return STATIC_CONF.equals(path) || path.endsWith("/"+STATIC_CONF);
  }
}

