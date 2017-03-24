package com.hubspot.blazar.base;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;

public class StepActivationCriteria implements Predicate<GitInfo> {
  private final Set<String> branches;

  @JsonCreator
  public StepActivationCriteria(@JsonProperty("branches") Set<String> branches) {
    this.branches = branches;
  }

  @Override
  public boolean apply(GitInfo input) {
    return branches.contains(input.getBranch());
  }

  public Set<String> getBranches() {
    return branches;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StepActivationCriteria that = (StepActivationCriteria) o;
    return Objects.equals(branches, that.branches);
  }

  @Override
  public int hashCode() {
    return Objects.hash(branches);
  }
}
