package com.hubspot.blazar.base;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfig {
  private List<String> cmds;
  private final Optional<Map<String, String>> env;
  private final Optional<List<String>> buildDependencies;
  private final Optional<List<String>> buildDependents;
  private final Optional<Boolean> autoDeployQa;

  @JsonCreator
  public BuildConfig(@JsonProperty("cmds") Optional<List<String>> cmds,
                     @JsonProperty("env") Optional<Map<String, String>> env,
                     @JsonProperty("buildDependencies") Optional<List<String>> buildDependencies,
                     @JsonProperty("buildDependents") Optional<List<String>> buildDependents,
                     @JsonProperty("autoDeployQa") Optional<Boolean> autoDeployQa) {
    this.env = env;
    if (cmds.isPresent()) {
      this.cmds = cmds.get();
    } else{
      this.cmds = ImmutableList.of("runbuildpack");
    }
    this.buildDependencies = buildDependencies;
    this.buildDependents = buildDependents;
    this.autoDeployQa = autoDeployQa;
  }

  public List<String> getCmds() {
    return cmds;
  }

  public Optional<Map<String, String>> getEnv() {
    return env;
  }

  public Optional<List<String>> getBuildDependencies() {
    return buildDependencies;
  }

  public Optional<List<String>> getBuildDependents() {
    return buildDependents;
  }

  public Optional<Boolean> getAutoDeployQa() {
    return autoDeployQa;
  }

}
