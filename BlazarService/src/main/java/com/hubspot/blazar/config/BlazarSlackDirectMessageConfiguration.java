package com.hubspot.blazar.config;

import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class BlazarSlackDirectMessageConfiguration {

  private final Set<String> whitelistedUserEmails;
  private final Set<String> blacklistedUserEmails;
  private final Set<String> ignoredBranches;

  /**
   * @param whitelistedUserEmails The list of users to directly slack when a build they pushed build fails
   * @param blacklistedUserEmails list of users not to slack when a build they pushed build fails
   * @param ignoredBranches The list of branches not to send direct message build alerts for. Useful to silence notifications for mass pull-requests.
   */
  @JsonCreator
  public BlazarSlackDirectMessageConfiguration(@JsonProperty("whitelistedUserEmails") Set<String> whitelistedUserEmails,
                                               @JsonProperty("blacklistedUserEmails") Set<String> blacklistedUserEmails,
                                               @JsonProperty("ignoredBranches") Set<String> ignoredBranches) {

    this.whitelistedUserEmails = MoreObjects.firstNonNull(whitelistedUserEmails, Collections.emptySet());
    this.blacklistedUserEmails = MoreObjects.firstNonNull(blacklistedUserEmails, Collections.emptySet());
    this.ignoredBranches = MoreObjects.firstNonNull(ignoredBranches, Collections.emptySet());
  }

  public Set<String> getWhitelistedUserEmails() {
    return whitelistedUserEmails;
  }

  public Set<String> getBlacklistedUserEmails() {
    return blacklistedUserEmails;
  }

  public Set<String> getIgnoredBranches() {
    return ignoredBranches;
  }
}
