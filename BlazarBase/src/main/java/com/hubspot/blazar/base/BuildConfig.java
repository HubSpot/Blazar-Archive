package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class BuildConfig {

  private final List<String> cmds;
  private final Map<String, String> env;
  private final List<String> buildDependencies;
  private final List<String> buildDependents;
  private final List<String> webHooks;

  @JsonCreator
  public BuildConfig(@JsonProperty("cmds") List<String> cmds,
                     @JsonProperty("env") Map<String, String> env,
                     @JsonProperty("buildDependencies") List<String> buildDependencies,
                     @JsonProperty("buildDependents") List<String> buildDependents,
                     @JsonProperty("webHooks") List<String> webHooks) {

    this.cmds = Objects.firstNonNull(cmds, ImmutableList.of("runbuildpack"));
    this.env = Objects.firstNonNull(env, Collections.<String,String>emptyMap());
    this.buildDependencies = Objects.firstNonNull(buildDependencies, Collections.<String>emptyList());
    this.buildDependents = Objects.firstNonNull(buildDependents, Collections.<String>emptyList());
    this.webHooks = Objects.firstNonNull(webHooks, Collections.<String>emptyList());
  }

  public List<String> getCmds() {
    return cmds;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public List<String> getBuildDependencies() {
    return buildDependencies;
  }

  public List<String> getBuildDependents() {
    return buildDependents;
  }

  public List<String> getWebHooks() {
    return webHooks;
  }
}
