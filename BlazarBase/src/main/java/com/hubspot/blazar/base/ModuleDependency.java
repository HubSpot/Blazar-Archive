package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleDependency {
  private final int moduleId;
  private final String name;

  @JsonCreator
  public ModuleDependency(@JsonProperty("moduleId") int moduleId, @JsonProperty("name") String name) {
    this.moduleId = moduleId;
    this.name = name;
  }

  public int getModuleId() {
    return moduleId;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("%s-%d", name, moduleId);
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
    return moduleId == that.moduleId && name.equalsIgnoreCase(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleId, name);
  }
}
