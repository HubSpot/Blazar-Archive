package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;

import java.util.Set;

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
}
