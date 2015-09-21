package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.ConfigParts.Buildpack;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

public class Module {
  private final Optional<Integer> id;
  private final String name;
  private final String path;
  private final String glob;
  private final PathMatcher matcher;
  private final boolean active;
  private final long updatedTimestamp;
  private final Optional<Buildpack> buildpack;

  public Module(String name, String path, String glob, Optional<Buildpack> buildpack) {
    this(Optional.<Integer>absent(), name, path, glob, true, System.currentTimeMillis(), buildpack);
  }

  @JsonCreator
  public Module(@JsonProperty("id") Optional<Integer> id,
                @JsonProperty("name") String name,
                @JsonProperty("path") String path,
                @JsonProperty("glob") String glob,
                @JsonProperty("active") boolean active,
                @JsonProperty("updatedTimestamp") long updatedTimestamp,
                @JsonProperty("buildpack") Optional<Buildpack> buildpack) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.glob = glob;
    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    this.active = active;
    this.updatedTimestamp = updatedTimestamp;
    this.buildpack = buildpack;
  }

  public Optional<Integer> getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public String getGlob() {
    return glob;
  }

  public boolean isActive() {
    return active;
  }

  public long getUpdatedTimestamp() {
    return updatedTimestamp;
  }

  public Optional<Buildpack> getBuildpack() {
    return buildpack;
  }

  public boolean contains(Path path) {
    return matcher.matches(path);
  }

  public Module withId(int id) {
    return new Module(Optional.of(id), name, path, glob, active, updatedTimestamp, buildpack);
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
        Objects.equals(path, module.path) &&
        Objects.equals(glob, module.glob);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, path, glob, active);
  }
}
