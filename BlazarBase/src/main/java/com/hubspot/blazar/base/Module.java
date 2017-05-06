package com.hubspot.blazar.base;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.hubspot.rosetta.annotations.StoredAsJson;

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
  @StoredAsJson
  private final Optional<BuildConfig> buildConfig;
  @StoredAsJson
  private final Optional<BuildConfig> resolvedBuildConfig;

  @JsonCreator
  public Module(@JsonProperty("id") Optional<Integer> id,
                @JsonProperty("name") String name,
                @JsonProperty("type") String type,
                @JsonProperty("path") String path,
                @JsonProperty("glob") String glob,
                @JsonProperty("active") boolean active,
                @JsonProperty("createdTimestamp") long createdTimestamp,
                @JsonProperty("updatedTimestamp") long updatedTimestamp,
                @JsonProperty("buildpack") Optional<GitInfo> buildpack,
                @JsonProperty("buildConfig") Optional<BuildConfig> buildConfig,
                @JsonProperty("resolvedBuildConfig") Optional<BuildConfig> resolvedBuildConfig) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.path = path;
    this.glob = Preconditions.checkNotNull(glob);
    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    this.active = active;
    this.createdTimestamp = createdTimestamp;
    this.updatedTimestamp = updatedTimestamp;
    this.buildpack = MoreObjects.firstNonNull(buildpack, Optional.<GitInfo>absent());
    this.buildConfig = MoreObjects.firstNonNull(buildConfig, Optional.absent());
    this.resolvedBuildConfig = MoreObjects.firstNonNull(buildConfig, Optional.absent());
  }

  public Module(Optional<Integer> id,
                String name,
                String type,
                String path,
                String glob,
                boolean active,
                long createdTimestamp,
                long updatedTimestamp,
                Optional<GitInfo> buildpack) {

    this(id, name, type, path, glob, active, createdTimestamp, updatedTimestamp, buildpack, Optional.absent(), Optional.absent());
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

  public Optional<BuildConfig> getBuildConfig() {
    return buildConfig;
  }

  public Optional<BuildConfig> getResolvedBuildConfig() {
    return resolvedBuildConfig;
  }

  public boolean contains(Path path) {
    return matcher.matches(path);
  }

  public Module withId(int id) {
    return new Module(Optional.of(id), name, type, path, glob, active, createdTimestamp, updatedTimestamp, buildpack, buildConfig, resolvedBuildConfig);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .add("type", type)
        .add("path", path)
        .add("glob", glob)
        .add("buildpack", buildpack)
        .add("buildConfig", buildConfig)
        .add("resolvedBuildConfig", resolvedBuildConfig)
        .toString();
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
        Objects.equals(buildpack, module.buildpack) &&
        Objects.equals(buildConfig, module.getBuildConfig()) &&
        Objects.equals(resolvedBuildConfig, module.getResolvedBuildConfig());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, path, glob, active, buildpack, buildConfig, resolvedBuildConfig);
  }
}
