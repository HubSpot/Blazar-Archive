package com.hubspot.blazar.base.metrics;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubspot.blazar.base.ModuleBuild;

public class ActiveModuleBuildsInState {

  private final ModuleBuild.State state;
  private final int count;

  public ActiveModuleBuildsInState(@JsonProperty("state") ModuleBuild.State state,
                                   @JsonProperty("count") int count) {
    this.state = state;
    this.count = count;
  }

  public ModuleBuild.State getState() {
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

    ActiveModuleBuildsInState pair = (ActiveModuleBuildsInState) o;
    return state.equals(pair.state) && count == pair.count;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, count);
  }
}
