package com.hubspot.blazar.base;

import java.util.Set;

public class BuildConfigDiscoveryResult {
  private final Set<DiscoveredBuildConfig> discoveredBuildConfigs;
  private final Set<MalformedFile> malformedFiles;

  public BuildConfigDiscoveryResult(Set<DiscoveredBuildConfig> discoveredBuildConfigs, Set<MalformedFile> malformedFiles) {
    this.discoveredBuildConfigs = discoveredBuildConfigs;
    this.malformedFiles = malformedFiles;
  }

  public Set<DiscoveredBuildConfig> getDiscoveredBuildConfigs() {
    return discoveredBuildConfigs;
  }

  public Set<MalformedFile> getMalformedFiles() {
    return malformedFiles;
  }
}
