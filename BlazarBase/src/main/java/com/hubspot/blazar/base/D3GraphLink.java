package com.hubspot.blazar.base;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class D3GraphLink {
  private final int target;
  private final int source;

  @JsonCreator
  public D3GraphLink(@JsonProperty("target") int target,
                     @JsonProperty("source") int source) {
    this.target = target;
    this.source = source;
  }

  public int getTarget() {
    return target;
  }

  public int getSource() {
    return source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    D3GraphLink link = (D3GraphLink) o;
    return Objects.equals(target, link.target) && Objects.equals(source, link.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target, source);
  }

}
