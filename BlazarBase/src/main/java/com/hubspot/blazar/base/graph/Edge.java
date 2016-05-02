package com.hubspot.blazar.base.graph;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Edge {
  private final int source;
  private final int target;

  @JsonCreator
  public Edge(@JsonProperty("source") int source, @JsonProperty("target") int target) {
    this.source = source;
    this.target = target;
  }

  public int getSource() {
    return source;
  }

  public int getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Edge edge = (Edge) o;
    return source == edge.source && target == edge.target;
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target);
  }
}
