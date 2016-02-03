package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DiscoveredModule extends Module {
  private final DependencyInfo dependencyInfo;

  public DiscoveredModule(String name,
                          String path,
                          String glob,
                          Optional<GitInfo> buildpack,
                          DependencyInfo dependencyInfo) {
    this(Optional.<Integer>absent(), name, path, glob, true, System.currentTimeMillis(), buildpack, dependencyInfo);
  }

  public DiscoveredModule(Optional<Integer> id,
                          String name,
                          String path,
                          String glob,
                          boolean active,
                          long updatedTimestamp,
                          Optional<GitInfo> buildpack,
                          DependencyInfo dependencyInfo) {
    super(id, name, path, glob, active, updatedTimestamp, buildpack);
    this.dependencyInfo = dependencyInfo;
  }

  @JsonIgnore
  public DependencyInfo getDependencyInfo() {
    return dependencyInfo;
  }

  @JsonIgnore
  public Set<ModuleDependency> getProvides() {
    Set<ModuleDependency> provides = new HashSet<>();
    for (String provided : dependencyInfo.getProvides()) {
      provides.add(new ModuleDependency(getId().get(), provided));
    }

    return provides;
  }

  @JsonIgnore
  public Set<ModuleDependency> getDepends() {
    Set<ModuleDependency> dependencies = new HashSet<>();
    for (String dependency : dependencyInfo.getDepends()) {
      dependencies.add(new ModuleDependency(getId().get(), dependency));
    }

    return dependencies;
  }

  @Override
  public DiscoveredModule withId(int id) {
    return new DiscoveredModule(
        Optional.of(id),
        getName(),
        getPath(),
        getGlob(),
        isActive(),
        getUpdatedTimestamp(),
        getBuildpack(),
        getDependencyInfo()
    );
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
