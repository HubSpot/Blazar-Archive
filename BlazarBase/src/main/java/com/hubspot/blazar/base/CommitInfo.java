package com.hubspot.blazar.base;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.hubspot.blazar.github.GitHubProtos.Commit;

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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("current", current)
        .add("previous", previous)
        .add("newCommits", newCommits)
        .add("truncated", truncated)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CommitInfo that = (CommitInfo) o;
    return Objects.equals(truncated, that.truncated) &&
        Objects.equals(current, that.current) &&
        Objects.equals(previous, that.previous) &&
        Objects.equals(newCommits, that.newCommits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(current, previous, newCommits, truncated);
  }
}
