package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BuildTrigger {
  public enum Type {
    PUSH, MANUAL, BRANCH_CREATION, INTER_PROJECT
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

  public static BuildTrigger forInterProjectBuild(GitInfo gitInfo) {
    String rootRepoBranch = String.format("%s-%s-%s-%s", gitInfo.getHost(), gitInfo.getOrganization(), gitInfo.getRepository(), gitInfo.getBranch());
    return new BuildTrigger(Type.INTER_PROJECT, rootRepoBranch);
  }

  public Type getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BuildTrigger that = (BuildTrigger) o;
    return Objects.equals(type, that.type) && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id);
  }
}
