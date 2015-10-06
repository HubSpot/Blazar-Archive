package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.Set;

public class DependencyInfo {
  private final Set<String> depends;
  private final Set<String> provides;

  public DependencyInfo(Set<String> depends, Set<String> provides) {
    this.depends = depends;
    this.provides = provides;
  }

  public static DependencyInfo unknown() {
    return new DependencyInfo(Collections.<String>emptySet(), Collections.<String>emptySet());
  }

  public Set<String> getDepends() {
    return depends;
  }

  public Set<String> getProvides() {
    return provides;
  }
}
