package com.hubspot.blazar.discovery;

import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Singleton
public class CompositeModuleDiscovery implements ModuleDiscovery {
  private final Set<ModuleDiscovery> delegates;

  @Inject
  public CompositeModuleDiscovery(Set<ModuleDiscovery> delegates) {
    this.delegates = delegates;
  }

  @Override
  public boolean allowDuplicates() {
    return true;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    for (ModuleDiscovery delegate : delegates) {
      if (delegate.shouldRediscover(gitInfo, pushEvent)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException {
    Map<String, Set<DiscoveredModule>> allModules = new HashMap<>();
    Map<String, Set<DiscoveredModule>> noDuplicates = new HashMap<>();

    for (ModuleDiscovery delegate : delegates) {
      final Map<String, Set<DiscoveredModule>> target;
      if (delegate.allowDuplicates()) {
        target = allModules;
      } else {
        target = noDuplicates;
      }

      for (DiscoveredModule module : delegate.discover(gitInfo)) {
        String folder = folderFor(module.getPath());

        Set<DiscoveredModule> modules = target.get(folder);
        if (modules == null) {
          modules = new HashSet<>();
          target.put(folder, modules);
        }

        modules.add(module);
      }
    }

    for (Entry<String, Set<DiscoveredModule>> entry : noDuplicates.entrySet()) {
      String folder = entry.getKey();
      Set<DiscoveredModule> modules = ImmutableSet.of(entry.getValue().iterator().next());

      if (!allModules.containsKey(folder)) {
        allModules.put(folder, modules);
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (Set<DiscoveredModule> folderModules : allModules.values()) {
      modules.addAll(folderModules);
    }

    return modules;
  }

  private static String folderFor(String path) {
    return path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "/";
  }
}
