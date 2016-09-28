package com.hubspot.blazar.base.metrics;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StateToActiveBuildCountPair {

  private final String stateName;
  private final Integer count;

  @JsonCreator
  public StateToActiveBuildCountPair(@JsonProperty("stateName") String stateName,
                                     @JsonProperty("count") Integer count) {
    this.stateName = stateName;
    this.count = count;
  }

  public String getStateName() {
    return stateName;
  }

  public Integer getCount() {
    return count;
  }

    @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StateToActiveBuildCountPair pair = (StateToActiveBuildCountPair) o;
    return Objects.equals(stateName, pair.stateName) && Objects.equals(count, pair.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stateName, count);
  }
}
