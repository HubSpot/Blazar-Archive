package com.hubspot.blazar.base.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class QueuedItemCount {

  private final String className;
  private final int count;

  @JsonCreator
  public QueuedItemCount(@JsonProperty("className") String className,
                        @JsonProperty("count") int count) {
    this.className = className;
    this.count = count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueuedItemCount that = (QueuedItemCount) o;
    return count == that.count &&
        Objects.equal(className, that.className);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(className, count);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("className", className)
        .add("count", count)
        .toString();
  }
}
