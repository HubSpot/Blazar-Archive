package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * Steps executed after a build, field of {@link BuildConfig}
 */
public class PostBuildSteps {
  private final List<BuildStep> onFailure;
  private final List<BuildStep> onSuccess;
  private final List<BuildStep> always;

  @JsonCreator
  public PostBuildSteps(@JsonProperty("onFailure") List<BuildStep> onFailure,
                        @JsonProperty("onSuccess") List<BuildStep> onSuccess,
                        @JsonProperty("always") List<BuildStep> always) {
    this.onFailure = MoreObjects.firstNonNull(onFailure, Collections.emptyList());
    this.onSuccess = MoreObjects.firstNonNull(onSuccess, Collections.emptyList());
    this.always = MoreObjects.firstNonNull(always, Collections.emptyList());
  }

  public List<BuildStep> getOnFailure() {
    return onFailure;
  }

  public List<BuildStep> getOnSuccess() {
    return onSuccess;
  }

  public List<BuildStep> getAlways() {
    return always;
  }
}
