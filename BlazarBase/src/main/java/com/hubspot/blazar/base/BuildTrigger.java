package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildTrigger {
  public enum Type {
    PUSH, MANUAL, BRANCH_CREATION
  }

  private final Type type;
  private final String id;

  @JsonCreator
  public BuildTrigger(@JsonProperty("type") Type type, @JsonProperty("id") String id) {
    this.type = type;
    this.id = id;
  }

  public static BuildTrigger forCommit(String sha) {
    return new BuildTrigger(Type.PUSH, sha);
  }

  public static BuildTrigger forUser(String user) {
    return new BuildTrigger(Type.MANUAL, user);
  }

  public static BuildTrigger forBranchCreation(String branch) {
    return new BuildTrigger(Type.BRANCH_CREATION, branch);
  }

  public Type getType() {
    return type;
  }

  public String getId() {
    return id;
  }

}
