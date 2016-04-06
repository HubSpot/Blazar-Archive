package com.hubspot.blazar.discovery.hsstatic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;

public class StaticConfig {

  private final String name;
  private final int majorVersion;
  private Map<String, Integer> runtimeDeps;
  private final Map<String, Integer> deps;

  @JsonCreator
  public StaticConfig(@JsonProperty("name") String name,
                      @JsonProperty("majorVersion") Optional<Integer> majorVersionCamel,
                      @JsonProperty("major_version") Optional<Integer> majorVersionUnderscore,
                      @JsonProperty("runtimeDeps") Map<String, String> runtimeDeps,
                      @JsonProperty("deps") Map<String, String> deps) {
    this.name = name;
    this.majorVersion = majorVersionCamel.or(majorVersionUnderscore).or(1);
    this.runtimeDeps = parseMajorVersions(runtimeDeps);
    this.deps = parseMajorVersions(deps);
  }

  public String getName() {
    return name;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public Map<String, Integer> getRuntimeDeps() {
    return runtimeDeps;
  }

  public Map<String, Integer> getDeps() {
    return deps;
  }

  private static Map<String, Integer> parseMajorVersions(@Nullable Map<String, String> versionMap) {
    if (versionMap == null) {
      return Collections.emptyMap();
    }

    return Maps.transformValues(versionMap, new Function<String, Integer>() {

      @Override
      public Integer apply(String input) {
        Set<Integer> majorVersions = new HashSet<>();
        for (String version : Splitter.on("||").trimResults().split(input)) {
          final int majorVersion;
          if (version.contains(".")) {
            majorVersion = Integer.valueOf(version.substring(0, version.indexOf('.')));
          } else {
            majorVersion = Integer.valueOf(version);
          }

          majorVersions.add(majorVersion);
        }

        return Collections.max(majorVersions);
      }
    });
  }
}
