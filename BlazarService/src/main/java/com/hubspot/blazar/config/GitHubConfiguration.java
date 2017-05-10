package com.hubspot.blazar.config;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public class GitHubConfiguration {
  private final Optional<String> user;
  private final Optional<String> password;
  private final Boolean setCommitStatus;
  private final Optional<String> oauthToken;
  private final List<String> organizations;

  /**
   * @param user The username to use when interacting with GitHub.
   * @param password The password to use.
   * @param setCommitStatus We don't want to post build statuses to all GitHub instances we configure.
   * @param oauthToken Alternate option to having a user/password you can use a user/token.
   * @param organizations A list of all the organizations that Blazar will pay attention to pushes from.
   */
  @JsonCreator
  public GitHubConfiguration(@JsonProperty("user") Optional<String> user,
                             @JsonProperty("password") Optional<String> password,
                             @JsonProperty("setCommitStatus") Optional<Boolean> setCommitStatus,
                             @JsonProperty("oauthToken") Optional<String> oauthToken,
                             @JsonProperty("organizations") List<String> organizations) {
    this.user = user;
    this.password = password;
    this.setCommitStatus = setCommitStatus.or(true);
    this.oauthToken = oauthToken;
    this.organizations = MoreObjects.firstNonNull(organizations, Collections.<String>emptyList());
  }

  public Optional<String> getUser() {
    return user;
  }

  public Optional<String> getPassword() {
    return password;
  }

  public boolean getSetCommitStatus() {
    return setCommitStatus;
  }

  public Optional<String> getOauthToken() {
    return oauthToken;
  }

  public List<String> getOrganizations() {
    return organizations;
  }
}
