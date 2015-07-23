package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Module {
  private final String name;
  private final String path;
  private final String basePath;

  @JsonCreator
  public Module(@JsonProperty("name") String name, @JsonProperty("path") String path) {
    this.name = name;
    this.path = path;
    this.basePath = path.contains("/") ? path.substring(0, path.lastIndexOf('/') + 1) : path;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public boolean contains(String path) {
    if (path.contains("/")) {
      return path.startsWith(basePath);
    } else {
      return path.equals(basePath);
    }
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
    return Objects.equals(name, module.name) && Objects.equals(path, module.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, path);
  }
}
