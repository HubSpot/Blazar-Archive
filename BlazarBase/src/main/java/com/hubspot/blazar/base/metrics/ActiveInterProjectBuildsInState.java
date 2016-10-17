package com.hubspot.blazar.base.metrics;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubspot.blazar.base.InterProjectBuild;

public class ActiveInterProjectBuildsInState {

  private final InterProjectBuild.State state;
  private final int count;

  public ActiveInterProjectBuildsInState(@JsonProperty("state") InterProjectBuild.State state,
                                         @JsonProperty("count") int count) {
    this.state = state;
    this.count = count;
  }

  public InterProjectBuild.State getState() {
    return this.state;
  }

  public int getCount() {
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

    ActiveInterProjectBuildsInState pair = (ActiveInterProjectBuildsInState) o;
    return state.equals(pair.state) && count == pair.count;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, count);
  }
}
