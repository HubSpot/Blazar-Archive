package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostBuildSteps that = (PostBuildSteps) o;
    return Objects.equals(onFailure, that.onFailure) &&
        Objects.equals(onSuccess, that.onSuccess) &&
        Objects.equals(always, that.always);
  }

  @Override
  public int hashCode() {
    return Objects.hash(onFailure, onSuccess, always);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("onFailure", onFailure)
        .add("onSuccess", onSuccess)
        .add("always", always)
        .toString();
  }
}
