package com.hubspot.blazar.discovery.hsstatic;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class StaticConfig {

  private final String name;
  private final int majorVersion;
  private final boolean isCurrentVersion;
  private Map<String, Integer> runtimeDeps;
  private final Map<String, Integer> deps;

  @JsonCreator
  public StaticConfig(@JsonProperty("name") String name,
                      @JsonProperty("majorVersion") int majorVersion,
                      @JsonProperty("isCurrentVersion") boolean isCurrentVersion,
                      @JsonProperty("runtimeDeps") Map<String, Integer> runtimeDeps,
                      @JsonProperty("deps") Map<String, Integer> deps) {
    this.name = name;
    this.majorVersion = majorVersion;
    this.isCurrentVersion = isCurrentVersion;
    this.runtimeDeps = Objects.firstNonNull(runtimeDeps, Collections.<String, Integer>emptyMap());
    this.deps = Objects.firstNonNull(deps, Collections.<String, Integer>emptyMap());
  }

  public String getName() {
    return name;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public boolean isCurrentVersion() {
    return isCurrentVersion;
  }

  public Map<String, Integer> getRuntimeDeps() {
    return runtimeDeps;
  }

  public Map<String, Integer> getDeps() {
    return deps;
  }
}
