package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.Set;

public class DependencyInfo {
  private final Set<Dependency> depends;
  private final Set<Dependency> provides;

  public DependencyInfo(Set<Dependency> depends, Set<Dependency> provides) {
    this.depends = depends;
    this.provides = provides;
  }

  public static DependencyInfo unknown() {
    return new DependencyInfo(Collections.emptySet(), Collections.emptySet());
  }

  public Set<Dependency> getDepends() {
    return depends;
  }

  public Set<Dependency> getProvides() {
    return provides;
  }
}
