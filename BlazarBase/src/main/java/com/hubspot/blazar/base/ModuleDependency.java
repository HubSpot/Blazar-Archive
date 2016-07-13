package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class ModuleDependency {
  private final int moduleId;
  private final String name;
  private String version;

  @JsonCreator
  public ModuleDependency(@JsonProperty("moduleId") int moduleId, @JsonProperty("name") String name, @JsonProperty("version") String version) {
    this.moduleId = moduleId;
    this.name = name;
    this.version = version;
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("moduleId", moduleId)
        .add("name", name)
        .add("version", version).toString();
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
    return moduleId == that.moduleId && name.equalsIgnoreCase(that.name) && version.equalsIgnoreCase(that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleId, name, version);
  }
}
