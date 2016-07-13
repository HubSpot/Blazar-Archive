package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.Set;

public class DependencyInfo {
  private final Set<DependencyItem> depends;
  private final Set<DependencyItem> provides;

  public DependencyInfo(Set<DependencyItem> depends, Set<DependencyItem> provides) {
    this.depends = depends;
    this.provides = provides;
  }

  public static DependencyInfo unknown() {
    return new DependencyInfo(Collections.emptySet(), Collections.emptySet());
  }

  public Set<DependencyItem> getDepends() {
    return depends;
  }

  public Set<DependencyItem> getProvides() {
    return provides;
  }
}
