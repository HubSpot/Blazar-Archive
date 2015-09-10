package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubspot.blazar.github.GitHubProtos.Commit;

import java.util.List;

public class CommitInfo {
  private final Commit current;
  private final List<Commit> newCommits;
  private final boolean truncated;

  @JsonCreator
  public CommitInfo(@JsonProperty("current") Commit current,
                    @JsonProperty("newCommits") List<Commit> newCommits,
                    @JsonProperty("truncated") boolean truncated) {
    this.current = current;
    this.newCommits = newCommits;
    this.truncated = truncated;
  }

  public Commit getCurrent() {
    return current;
  }

  public List<Commit> getNewCommits() {
    return newCommits;
  }

  public boolean isTruncated() {
    return truncated;
  }
}
