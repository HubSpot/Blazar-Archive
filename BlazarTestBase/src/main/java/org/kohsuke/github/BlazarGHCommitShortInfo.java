package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHCommitShortInfo extends GHCommit.ShortInfo {
  private final String message;
  private final BlazarGitUser author;
  private final BlazarGitUser commiter;

  @JsonCreator
  public BlazarGHCommitShortInfo(@JsonProperty("message") String message,
                                 @JsonProperty("author") BlazarGitUser author,
                                 @JsonProperty("commiter") BlazarGitUser commiter) {
    this.message = message;
    this.author = author;
    this.commiter = commiter;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public GitUser getCommitter() {
    return this.commiter;
  }

  @Override
  public GitUser getAuthor() {
    return this.author;
  }
}
