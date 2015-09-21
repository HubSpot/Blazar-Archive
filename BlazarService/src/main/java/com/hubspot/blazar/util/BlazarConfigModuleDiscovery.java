package com.hubspot.blazar.util;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class BlazarConfigModuleDiscovery extends AbstractModuleDiscovery {
  private final ObjectMapper mapper;
  private final YAMLFactory yamlFactory;

  @Inject
  public BlazarConfigModuleDiscovery(ObjectMapper mapper, YAMLFactory yamlFactory) {
    this.mapper = mapper;
    this.yamlFactory = yamlFactory;
  }

  @Override
  public boolean allowDuplicates() {
    return false;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    for (String path : affectedPaths(pushEvent)) {
      if (isBlazarConfig(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Set<Module> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = repositoryFor(gitInfo);
    GHTree tree = treeFor(repository, gitInfo);

    Set<String> blazarConfigs = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isBlazarConfig(entry.getPath())) {
        blazarConfigs.add(entry.getPath());
      }
    }

    Set<Module> modules = new HashSet<>();
    for (String blazarConfig : blazarConfigs) {
      BuildConfig buildConfig = getBuildConfigFromString(contentsFor(blazarConfig, repository, gitInfo));
      if (canBuild(buildConfig)) {
        String moduleName = moduleName(gitInfo, blazarConfig);
        String glob = (blazarConfig.contains("/") ? blazarConfig.substring(0, blazarConfig.lastIndexOf('/') + 1) : "") + "**";
        modules.add(new Module(moduleName, blazarConfig, glob, buildConfig.getBuildpack()));
      }
    }
    return modules;
  }

  private BuildConfig getBuildConfigFromString(String configString) throws IOException {
    return mapper.readValue(yamlFactory.createParser(configString), BuildConfig.class);
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
