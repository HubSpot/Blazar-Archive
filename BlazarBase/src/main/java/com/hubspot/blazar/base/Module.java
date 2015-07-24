package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class Module {
  private final Optional<Long> id;
  private final String name;
  private final String path;
  private final String basePath;
  private final boolean active;

  @JsonCreator
  public Module(@JsonProperty("id") Optional<Long> id,
                @JsonProperty("name") String name,
                @JsonProperty("path") String path,
                @JsonProperty("active") boolean active) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.basePath = path.contains("/") ? path.substring(0, path.lastIndexOf('/') + 1) : path;
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

  public boolean isActive() {
    return active;
  }

  public boolean contains(String path) {
    if (path.contains("/")) {
      return path.startsWith(basePath);
    } else {
      return path.equals(basePath);
    }
  }

  public Module withId(long id) {
    return new Module(Optional.of(id), name, path, active);
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
        Objects.equals(basePath, module.basePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, path, basePath, active);
  }
}
