package com.hubspot.blazar.base;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class D3GraphNode {
  private final String name;
  private int moduleId;
  private final int width;
  private final int height;
  private final InterProjectBuild.State state;

  @JsonCreator
  public D3GraphNode(@JsonProperty("name") String name,
                     @JsonProperty("moduleId") int moduleId,
                     @JsonProperty("width") int width,
                     @JsonProperty("height") int height,
                     @JsonProperty("state") InterProjectBuild.State state) {
    this.name = name;
    this.moduleId = moduleId;
    this.width = width;
    this.height = height;
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public int getModuleId() {
    return moduleId;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public InterProjectBuild.State getState() {
    return state;
  }
}
