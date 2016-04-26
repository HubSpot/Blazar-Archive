package org.kohsuke.github;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGitUser extends GitUser {

  private final String login;
  private final String email;
  private final Date date;

  public BlazarGitUser(@JsonProperty("login") String login,
                       @JsonProperty("email") String email,
                       @JsonProperty("timestamp") long timestamp) {
    this.login = login;
    this.email = email;
    this.date = new Date(timestamp);
  }

  @Override
  public String getName() {
    return this.login;
  }

  @Override
  public String getEmail() {
    return this.email;
  }

  @Override
  public Date getDate() {
    return this.date;
  }

  public static BlazarGHUser toGHUser(GitUser user){
    return new BlazarGHUser(user.getName(), user.getName(), user.getEmail());
  }
}
