package com.hubspot.blazar.util;

import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
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
  public Set<Module> discover(GitInfo gitInfo) throws IOException {
    Map<String, Set<Module>> allModules = new HashMap<>();
    Map<String, Set<Module>> noDuplicates = new HashMap<>();

    for (ModuleDiscovery delegate : delegates) {
      final Map<String, Set<Module>> target;
      if (delegate.allowDuplicates()) {
        target = allModules;
      } else {
        target = noDuplicates;
      }

      for (Module module : delegate.discover(gitInfo)) {
        String folder = folderFor(module.getPath());

        Set<Module> modules = target.get(folder);
        if (modules == null) {
          modules = new HashSet<>();
          target.put(folder, modules);
        }

        modules.add(module);
      }
    }

    for (Entry<String, Set<Module>> entry : noDuplicates.entrySet()) {
      String folder = entry.getKey();
      Set<Module> modules = ImmutableSet.of(entry.getValue().iterator().next());

      if (!allModules.containsKey(folder)) {
        allModules.put(folder, modules);
      }
    }

    Set<Module> modules = new HashSet<>();
    for (Set<Module> folderModules : allModules.values()) {
      modules.addAll(folderModules);
    }

    return modules;
  }

  private static String folderFor(String path) {
    return path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "/";
  }
}
