package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class BuildConfig {
  private final List<String> cmds;
  private final Map<String, String> env;
  private final List<String> buildDeps;
  private final List<String> webhooks;
  private final Optional<GitInfo> buildpack;

  @JsonCreator
  public BuildConfig(@JsonProperty("cmds") List<String> cmds,
                     @JsonProperty("env") Map<String, String> env,
                     @JsonProperty("buildDeps") List<String> buildDeps,
                     @JsonProperty("webhooks") List<String> webhooks,
                     @JsonProperty("buildpack") Optional<GitInfo> buildpack) {

    this.cmds = Objects.firstNonNull(cmds, Collections.<String>emptyList());
    this.env = Objects.firstNonNull(env, Collections.<String,String>emptyMap());
    this.buildDeps = Objects.firstNonNull(buildDeps, Collections.<String>emptyList());
    this.webhooks = Objects.firstNonNull(webhooks, Collections.<String>emptyList());
    this.buildpack = buildpack;
  }

  public static BuildConfig makeDefaultBuildConfig(){
    return new BuildConfig(null, null, null, null, Optional.<GitInfo>absent());
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

  public Optional<GitInfo> getBuildpack() {
    return buildpack;
  }
}
