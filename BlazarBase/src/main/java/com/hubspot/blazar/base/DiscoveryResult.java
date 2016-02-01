package com.hubspot.blazar.base;

import java.util.Set;

public class DiscoveryResult {
  private final Set<DiscoveredModule> modules;
  private final Set<MalformedFile> malformedFiles;

  public DiscoveryResult(Set<DiscoveredModule> modules, Set<MalformedFile> malformedFiles) {
    this.modules = modules;
    this.malformedFiles = malformedFiles;
  }

  public Set<DiscoveredModule> getModules() {
    return modules;
  }

  public Set<MalformedFile> getMalformedFiles() {
    return malformedFiles;
  }
}
