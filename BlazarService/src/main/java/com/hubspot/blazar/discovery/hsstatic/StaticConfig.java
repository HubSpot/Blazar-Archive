package com.hubspot.blazar.discovery.hsstatic;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StaticConfig {

  private final String name;
  private final int majorVersion;
  private final boolean isCurrentVersion;
  private final Map<String, Integer> deps;

  @JsonCreator
  public StaticConfig(@JsonProperty("name") String name,
                      @JsonProperty("majorVersion") int majorVersion,
                      @JsonProperty("isCurrentVersion") boolean isCurrentVersion,
                      @JsonProperty("deps") Map<String, Integer> deps) {
    this.name = name;
    this.majorVersion = majorVersion;
    this.isCurrentVersion = isCurrentVersion;
    this.deps = deps;
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

  public Map<String, Integer> getDeps() {
    return deps;
  }
}
