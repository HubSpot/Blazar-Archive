package org.kohsuke.github;

import java.io.IOException;

import org.kohsuke.github.GHUser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHUser extends GHUser {

  private final String name;
  private final String login;
  private final String email;

  @JsonCreator
  public BlazarGHUser(@JsonProperty("name") String name,
                      @JsonProperty("login") String login,
                      @JsonProperty("email") String email) {
    this.name = name;
    this.login = login;
    this.email = email;
  }

  @Override
  public String getName() throws IOException {
    return this.name;
  }

  @Override
  public String getLogin() {
    return this.login;
  }

  @Override
  public String getEmail() throws IOException {
    return this.email;
  }
}
