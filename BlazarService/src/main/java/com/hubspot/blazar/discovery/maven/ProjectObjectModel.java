package com.hubspot.blazar.discovery.maven;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.hubspot.blazar.base.DependencyInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectObjectModel {
  private final String groupId;
  private final String artifactId;
  private final String packaging;
  private final List<Map<String, String>> dependencies;
  private final Set<String> depends;

  @JsonCreator
  public ProjectObjectModel(@JsonProperty("parent") Map<String, String> parent,
                            @JsonProperty("groupId") Optional<String> groupId,
                            @JsonProperty("artifactId") String artifactId,
                            @JsonProperty("packaging") Optional<String> packaging,
                            @JsonProperty("dependencies") List<Map<String, String>> dependencies) {
    this.groupId = Preconditions.checkNotNull(groupId.or(parent.get("groupId")));
    this.artifactId = Preconditions.checkNotNull(artifactId);
    this.packaging = packaging.or("jar");
    this.dependencies = Objects.firstNonNull(dependencies, Collections.<Map<String, String>>emptyList());
    this.depends = toDepends(dependencies);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getPackaging() {
    return packaging;
  }

  public List<Map<String, String>> getDependencies() {
    return dependencies;
  }

  @JsonIgnore
  public DependencyInfo getDependencyInfo() {
    return new DependencyInfo(getDepends(), getProvides());
  }

  @JsonIgnore
  public Set<String> getDepends() {
    return depends;
  }

  @JsonIgnore
  public Set<String> getProvides() {
    return Collections.singleton(groupId + ":" + artifactId);
  }

  private static Set<String> toDepends(List<Map<String, String>> dependencies) {
    Set<String> depends = new HashSet<>();

    if (dependencies == null) {
      return depends;
    }

    for (Map<String, String> dependency : dependencies) {
      String groupId = Preconditions.checkNotNull(dependency.get("groupId"));
      String artifactId = Preconditions.checkNotNull(dependency.get("artifactId"));
      if (groupId.contains("${") || artifactId.contains("${")) {
        continue;
      }

      depends.add(groupId + ":" + artifactId);
    }

    return depends;
  }
}
