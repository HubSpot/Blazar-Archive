package com.hubspot.blazar.base;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;

public class DiscoveredModule extends Module {
  private final DependencyInfo dependencyInfo;

  public DiscoveredModule(String name,
                          String type,
                          String path,
                          String glob,
                          Optional<GitInfo> buildpack,
                          DependencyInfo dependencyInfo,
                          Optional<BuildConfig> buildConfig,
                          Optional<BuildConfig> resolvedBuildConfig) {
    this(Optional.<Integer>absent(), name, type, path, glob, true, System.currentTimeMillis(),
        System.currentTimeMillis(), buildpack, dependencyInfo, buildConfig, resolvedBuildConfig);
  }

  public DiscoveredModule(Optional<Integer> id,
                          String name,
                          String type,
                          String path,
                          String glob,
                          boolean active,
                          long createdTimestamp,
                          long updatedTimestamp,
                          Optional<GitInfo> buildpack,
                          DependencyInfo dependencyInfo,
                          Optional<BuildConfig> buildConfig,
                          Optional<BuildConfig> resolvedBuildConfig) {
    super(id, name, type, path, glob, active, createdTimestamp, updatedTimestamp, buildpack, buildConfig, resolvedBuildConfig);
    this.dependencyInfo = dependencyInfo;
  }

  public DependencyInfo getDependencyInfo() {
    return dependencyInfo;
  }

  @JsonIgnore
  public Set<ModuleDependency> getProvides() {
    Set<ModuleDependency> provides = new HashSet<>();
    for (Dependency provided : dependencyInfo.getProvides()) {
      provides.add(new ModuleDependency(getId().get(), provided.getName(), provided.getVersion()));
    }

    return provides;
  }

  @JsonIgnore
  public Set<ModuleDependency> getDepends() {
    Set<ModuleDependency> dependencies = new HashSet<>();
    for (Dependency dependency : dependencyInfo.getDepends()) {
      dependencies.add(new ModuleDependency(getId().get(), dependency.getName(), dependency.getVersion()));
    }

    return dependencies;
  }

  @Override
  public DiscoveredModule withId(int id) {
    return new DiscoveredModule(
        Optional.of(id),
        getName(),
        getType(),
        getPath(),
        getGlob(),
        isActive(),
        getCreatedTimestamp(),
        getUpdatedTimestamp(),
        getBuildpack(),
        getDependencyInfo(),
        getBuildConfig(),
        getResolvedBuildConfig());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DiscoveredModule that = (DiscoveredModule) o;
    return super.equals(o) && Objects.equals(dependencyInfo, that.dependencyInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), dependencyInfo);
  }
}
