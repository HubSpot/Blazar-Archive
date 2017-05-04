package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class ModuleDependency {
  public enum Source {
    PLUGIN,
    BUILD_CONFIG
  }
  private final int moduleId;
  private final String name;
  private final String version;
  private final Source source;

  @JsonCreator
  public ModuleDependency(@JsonProperty("moduleId") int moduleId,
                          @JsonProperty("name") String name,
                          @JsonProperty("version") String version,
                          @JsonProperty("source") Source source) {

    this.moduleId = moduleId;
    this.name = name;
    this.version = version;
    this.source = source;
  }

  public int getModuleId() {
    return moduleId;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public Source getSource() {
    return source;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("moduleId", moduleId)
        .add("name", name)
        .add("version", version)
        .add("source", source)
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

    ModuleDependency that = (ModuleDependency) o;
    return moduleId == that.moduleId &&
        name.equalsIgnoreCase(that.name) &&
        version.equalsIgnoreCase(that.version) &&
        source == that.source;
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleId, name, version, source);
  }
}
