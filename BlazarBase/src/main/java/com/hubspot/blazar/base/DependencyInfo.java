package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.Set;

import com.google.common.base.MoreObjects;

public class DependencyInfo {
  private final Set<Dependency> buildConfigDependencies;
  private final Set<Dependency> buildConfigProvidedDependencies;
  private final Set<Dependency> pluginDiscoveredDependencies;
  private final Set<Dependency> pluginDiscoveredProvidedDependencies;

  public DependencyInfo(Set<Dependency> buildConfigDependencies, Set<Dependency> buildConfigProvidedDependencies, Set<Dependency> pluginDiscoveredDependencies, Set<Dependency> pluginDiscoveredProvidedDependencies) {
    this.buildConfigDependencies = MoreObjects.firstNonNull(buildConfigDependencies, Collections.emptySet());
    this.buildConfigProvidedDependencies = MoreObjects.firstNonNull(buildConfigProvidedDependencies, Collections.emptySet());
    this.pluginDiscoveredDependencies = MoreObjects.firstNonNull(pluginDiscoveredDependencies, Collections.emptySet());
    this.pluginDiscoveredProvidedDependencies = MoreObjects.firstNonNull(pluginDiscoveredProvidedDependencies, Collections.emptySet());
  }

  public static DependencyInfo unknown() {
    return new DependencyInfo(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
  }

  public Set<Dependency> getBuildConfigDependencies() {
    return buildConfigDependencies;
  }

  public Set<Dependency> getBuildConfigProvidedDependencies() {
    return buildConfigProvidedDependencies;
  }

  public Set<Dependency> getPluginDiscoveredDependencies() {
    return pluginDiscoveredDependencies;
  }

  public Set<Dependency> getPluginDiscoveredProvidedDependencies() {
    return pluginDiscoveredProvidedDependencies;
  }
}
