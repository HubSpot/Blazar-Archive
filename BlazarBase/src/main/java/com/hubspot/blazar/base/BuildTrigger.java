package com.hubspot.blazar.base;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

public class BuildTrigger {
  public enum Type {
    PUSH, MANUAL, BRANCH_CREATION
  }

  private final Type type;
  private final String id;
  private final Set<Integer> moduleIds;

  @JsonCreator
  public BuildTrigger(@JsonProperty("type") Type type, @JsonProperty("id") String id, @JsonProperty("moduleIds") Set<Integer> moduleIds) {
    this.type = type;
    this.id = id;
    this.moduleIds = moduleIds;
  }

  public static BuildTrigger forCommit(String sha) {
    return new BuildTrigger(Type.PUSH, sha, ImmutableSet.<Integer>of());
  }

  public static BuildTrigger forUser(String user, Set<Integer> moduleIds) {
    return new BuildTrigger(Type.MANUAL, user, moduleIds);
  }

  public static BuildTrigger forBranchCreation(String branch) {
    return new BuildTrigger(Type.BRANCH_CREATION, branch, ImmutableSet.<Integer>of());
  }

  public Type getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public Set<Integer> getModuleIds() {
    return moduleIds;
  }

}
