package com.hubspot.blazar.util;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.github.GitHubProtos.Commit;
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
    for (Commit commit : pushEvent.getCommitsList()) {
      for (String file : commit.getAddedList()) {
        if (isBlazarConfig(file)) {
          return true;
        }
      }

      for (String file : commit.getRemovedList()) {
        if (isBlazarConfig(file)) {
          return true;
        }
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
      if (hasCommandsSection(contentsFor(blazarConfig, repository, gitInfo))) {
        String moduleName = moduleName(gitInfo, blazarConfig);
        String glob = (blazarConfig.contains("/") ? blazarConfig.substring(0, blazarConfig.lastIndexOf('/') + 1) : "") + "**";
        modules.add(new Module(Optional.<Integer>absent(), moduleName, blazarConfig, glob, true));
      }
    }

    return modules;
  }

  private boolean hasCommandsSection(String config) throws IOException {
    TreeNode node = mapper.readTree(yamlFactory.createParser(config));
    return node.get("cmds") != null;
  }

  private static String moduleName(GitInfo gitInfo, String path) {
    return path.contains("/") ? folderName(path) : gitInfo.getRepository();
  }

  private static String folderName(String path) {
    return path.substring(path.lastIndexOf('/') + 1);
  }

  private static boolean isBlazarConfig(String path) {
    return ".blazar.yaml".equals(path) || path.endsWith("/.blazar.yaml");
  }
}
