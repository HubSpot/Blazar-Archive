package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Commit {
  private final String sha;
  private final String message;
  private final User author;
  private final User committer;

  @JsonCreator
  public Commit(@JsonProperty("sha") String sha,
                @JsonProperty("message") String message,
                @JsonProperty("author") User author,
                @JsonProperty("committer") User committer) {
    this.sha = sha;
    this.message = message;
    this.author = author;
    this.committer = committer;
  }

  public String getSha() {
    return sha;
  }

  public String getMessage() {
    return message;
  }

  public User getAuthor() {
    return author;
  }

  public User getCommitter() {
    return committer;
  }

  private static class User {
    private final String name;
    private final String email;
    private final long commitTimestamp;

    @JsonCreator
    public User(@JsonProperty("name") String name,
                @JsonProperty("email") String email,
                @JsonProperty("commitTimestamp") long commitTimestamp) {
      this.name = name;
      this.email = email;
      this.commitTimestamp = commitTimestamp;
    }

    public String getName() {
      return name;
    }

    public String getEmail() {
      return email;
    }

    public long getCommitTimestamp() {
      return commitTimestamp;
    }
  }
}
