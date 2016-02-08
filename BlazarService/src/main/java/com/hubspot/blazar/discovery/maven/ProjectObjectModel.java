package com.hubspot.blazar.discovery.maven;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.hubspot.blazar.base.DependencyInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectObjectModel {
  private final String groupId;
  private final String artifactId;
  private final String packaging;
  private final List<Map<String, Object>> dependencies;
  private final Set<String> depends;

  @JsonCreator
  public ProjectObjectModel(@JsonProperty("parent") final Map<String, Object> parent,
                            @JsonProperty("groupId") Optional<String> groupId,
                            @JsonProperty("artifactId") String artifactId,
                            @JsonProperty("packaging") Optional<String> packaging,
                            @JsonProperty("dependencies") List<Map<String, Object>> dependencies) {
    this.groupId = groupId.or(new Supplier<String>() {

      @Override
      public String get() {
        return (String) parent.get("groupId");
      }
    });
    this.artifactId = Preconditions.checkNotNull(artifactId);
    this.packaging = packaging.or("jar");
    this.dependencies = Objects.firstNonNull(dependencies, new ArrayList<Map<String, Object>>());
    if (parent != null) {
      this.dependencies.add(parent);
    }
    this.depends = toDepends(this.dependencies);
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

  public List<Map<String, Object>> getDependencies() {
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

  private static Set<String> toDepends(List<Map<String, Object>> dependencies) {
    Set<String> depends = new HashSet<>();

    if (dependencies == null) {
      return depends;
    }

    for (Map<String, Object> dependency : dependencies) {
      String groupId = Preconditions.checkNotNull((String) dependency.get("groupId"));
      String artifactId = Preconditions.checkNotNull((String) dependency.get("artifactId"));
      if (groupId.contains("${") || artifactId.contains("${")) {
        continue;
      }

      depends.add(groupId + ":" + artifactId);
    }

    return depends;
  }
}
