package com.hubspot.blazar.discovery;

import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class CompositeModuleDiscovery implements ModuleDiscovery {
  private final Set<ModuleDiscovery> delegates;
  private final BlazarConfigModuleDiscovery configDiscovery;

  @Inject
  public CompositeModuleDiscovery(Set<ModuleDiscovery> delegates, BlazarConfigModuleDiscovery configDiscovery) {
    this.delegates = delegates;
    this.configDiscovery = configDiscovery;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (ModuleDiscovery delegate : delegates) {
      if (delegate.shouldRediscover(gitInfo, commitInfo)) {
        return true;
      }
    }

    return configDiscovery.shouldRediscover(gitInfo, commitInfo);
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    Map<String, Set<DiscoveredModule>> modulesByPath = new HashMap<>();
    Set<MalformedFile> malformedFiles = new HashSet<>();

    for (ModuleDiscovery delegate : delegates) {
      DiscoveryResult result  =delegate.discover(gitInfo);
      malformedFiles.addAll(result.getMalformedFiles());
      for (DiscoveredModule module : result.getModules()) {
        String folder = module.getFolder();

        Set<DiscoveredModule> modules = modulesByPath.get(folder);
        if (modules == null) {
          modules = new HashSet<>();
          modulesByPath.put(folder, modules);
        }

        modules.add(module);
      }
    }

    DiscoveryResult result = configDiscovery.discover(gitInfo);
    malformedFiles.addAll(result.getMalformedFiles());
    for (DiscoveredModule module : result.getModules()) {
      String folder = module.getFolder();

      if (!module.isActive()) {
        modulesByPath.remove(folder);
      } else if (!modulesByPath.containsKey(folder)) {
        modulesByPath.put(folder, ImmutableSet.of(module));
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (Set<DiscoveredModule> folderModules : modulesByPath.values()) {
      modules.addAll(folderModules);
    }

    return new DiscoveryResult(modules, malformedFiles);
  }
}
