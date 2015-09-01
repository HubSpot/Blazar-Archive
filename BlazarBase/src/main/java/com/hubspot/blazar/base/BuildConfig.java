package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class BuildConfig {
  private final List<String> cmds;
  private final Map<String, String> env;
  private final List<String> buildDeps;
  private final List<String> webhooks;

  @JsonCreator
  public BuildConfig(@JsonProperty("cmds") List<String> cmds,
                     @JsonProperty("env") Map<String, String> env,
                     @JsonProperty("buildDeps") List<String> buildDeps,
                     @JsonProperty("webhooks") List<String> webhooks) {

    this.cmds = Objects.firstNonNull(cmds, ImmutableList.of("runbuildpack"));
    this.env = Objects.firstNonNull(env, Collections.<String,String>emptyMap());
    this.buildDeps = Objects.firstNonNull(buildDeps, Collections.<String>emptyList());
    this.webhooks = Objects.firstNonNull(webhooks, Collections.<String>emptyList());
  }

  public static BuildConfig makeDefaultBuildConfig(){
    return new BuildConfig(null, null, null, null);
  }

  public List<String> getCmds() {
    return cmds;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public List<String> getBuildDeps() {
    return buildDeps;
  }

  public List<String> getWebhooks() {
    return webhooks;
  }
}
