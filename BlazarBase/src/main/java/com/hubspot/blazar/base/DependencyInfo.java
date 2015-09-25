package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class DependencyInfo {
  private final Set<String> depends;
  private final Set<String> provides;

  @JsonCreator
  public DependencyInfo(@JsonProperty("depends") Set<String> depends,
                        @JsonProperty("provides") Set<String> provides) {
    this.depends = depends;
    this.provides = provides;
  }

  public Set<String> getDepends() {
    return depends;
  }

  public Set<String> getProvides() {
    return provides;
  }
}
