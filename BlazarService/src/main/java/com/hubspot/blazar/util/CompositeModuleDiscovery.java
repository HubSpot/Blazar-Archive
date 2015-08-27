package com.hubspot.blazar.util;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class CompositeModuleDiscovery implements ModuleDiscovery {
  private final Set<ModuleDiscovery> delegates;

  @Inject
  public CompositeModuleDiscovery(Set<ModuleDiscovery> delegates) {
    this.delegates = delegates;
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
    Set<Module> modules = new HashSet<>();
    for (ModuleDiscovery delegate : delegates) {
      modules.addAll(delegate.discover(gitInfo));
    }

    return modules;
  }
}
