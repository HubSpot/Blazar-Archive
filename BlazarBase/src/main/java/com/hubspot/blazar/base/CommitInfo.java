package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubspot.blazar.github.GitHubProtos.Commit;

import java.util.List;

public class CommitInfo {
  private final Commit current;
  private final List<Commit> newCommits;

  @JsonCreator
  public CommitInfo(@JsonProperty("current") Commit current,
                    @JsonProperty("newCommits") List<Commit> newCommits) {
    this.current = current;
    this.newCommits = newCommits;
  }

  public Commit getCurrent() {
    return current;
  }

  public List<Commit> getNewCommits() {
    return newCommits;
  }
}
