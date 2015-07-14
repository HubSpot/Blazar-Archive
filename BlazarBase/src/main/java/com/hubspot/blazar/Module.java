package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Module {
  private final String name;
  private final String path;

  @JsonCreator
  public Module(@JsonProperty("name") String name, @JsonProperty("path") String path) {
    this.name = name;
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }
}
