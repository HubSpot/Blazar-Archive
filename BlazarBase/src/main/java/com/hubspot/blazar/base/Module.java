package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

public class Module {
  private final Optional<Long> id;
  private final String name;
  private final String path;
  private final String glob;
  private final PathMatcher matcher;
  private final boolean active;

  @JsonCreator
  public Module(@JsonProperty("id") Optional<Long> id,
                @JsonProperty("name") String name,
                @JsonProperty("path") String path,
                @JsonProperty("glob") String glob,
                @JsonProperty("active") boolean active) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.glob = glob;
    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
    this.active = active;
  }

  public Optional<Long> getId() {
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

  public boolean contains(Path path) {
    return matcher.matches(path);
  }

  public Module withId(long id) {
    return new Module(Optional.of(id), name, path, glob, active);
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
        Objects.equals(glob, module.glob) &&
        Objects.equals(matcher, module.matcher);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, path, glob, matcher, active);
  }
}
