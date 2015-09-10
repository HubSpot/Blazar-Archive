package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.blazar.github.GitHubProtos.Commit;

import java.util.List;

public class CommitInfo {
  private final Commit current;
  private final Optional<Commit> previous;
  private final List<Commit> newCommits;
  private final boolean truncated;

  @JsonCreator
  public CommitInfo(@JsonProperty("current") Commit current,
                    @JsonProperty("previous") Optional<Commit> previous,
                    @JsonProperty("newCommits") List<Commit> newCommits,
                    @JsonProperty("truncated") boolean truncated) {
    this.current = current;
    this.previous = previous;
    this.newCommits = newCommits;
    this.truncated = truncated;
  }

  public Commit getCurrent() {
    return current;
  }

  public Optional<Commit> getPrevious() {
    return previous;
  }

  public List<Commit> getNewCommits() {
    return newCommits;
  }

  public boolean isTruncated() {
    return truncated;
  }
}
