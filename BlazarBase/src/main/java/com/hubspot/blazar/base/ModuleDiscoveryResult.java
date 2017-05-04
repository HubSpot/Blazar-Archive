package com.hubspot.blazar.base;

import java.util.Set;

public class ModuleDiscoveryResult {
  private final Set<Module> modules;
  private final Set<MalformedFile> malformedFiles;

  public ModuleDiscoveryResult(Set<Module> modules, Set<MalformedFile> malformedFiles) {
    this.modules = modules;
    this.malformedFiles = malformedFiles;
  }

  public Set<Module> getModules() {
    return modules;
  }

  public Set<MalformedFile> getMalformedFiles() {
    return malformedFiles;
  }
}
