package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.rosetta.annotations.StoredAsJson;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

public class Module {
  private final Optional<Integer> id;
  private final String name;
  private final String type;
  private final String path;
  private final String glob;
  private final PathMatcher matcher;
  private final boolean active;
  private final long createdTimestamp;
  private final long updatedTimestamp;
  @StoredAsJson
  private final Optional<GitInfo> buildpack;

  @JsonCreator
  public Module(@JsonProperty("id") Optional<Integer> id,
                @JsonProperty("name") String name,
                @JsonProperty("type") String type,
                @JsonProperty("path") String path,
                @JsonProperty("glob") String glob,
                @JsonProperty("active") boolean active,
                @JsonProperty("createdTimestamp") long createdTimestamp,
                @JsonProperty("updatedTimestamp") long updatedTimestamp,
                @JsonProperty("buildpack") Optional<GitInfo> buildpack) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.path = path;
    this.glob = glob;
    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    this.active = active;
    this.createdTimestamp = createdTimestamp;
    this.updatedTimestamp = updatedTimestamp;
    this.buildpack = com.google.common.base.Objects.firstNonNull(buildpack, Optional.<GitInfo>absent());
  }

  public Optional<Integer> getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getPath() {
    return path;
  }

  @JsonIgnore
  public String getFolder() {
    return path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
  }

  public String getGlob() {
    return glob;
  }

  public boolean isActive() {
    return active;
  }

  public long getCreatedTimestamp() {
    return createdTimestamp;
  }

  public long getUpdatedTimestamp() {
    return updatedTimestamp;
  }

  public Optional<GitInfo> getBuildpack() {
    return buildpack;
  }

  public boolean contains(Path path) {
    return matcher.matches(path);
  }

  public Module withId(int id) {
    return new Module(Optional.of(id), name, type, path, glob, active, createdTimestamp, updatedTimestamp, buildpack);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Module module = (Module) o;
    return Objects.equals(active, module.active) &&
        Objects.equals(id, module.id) &&
        Objects.equals(name, module.name) &&
        Objects.equals(type, module.type) &&
        Objects.equals(path, module.path) &&
        Objects.equals(glob, module.glob) &&
        Objects.equals(buildpack, module.buildpack);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, path, glob, active, buildpack);
  }
}
