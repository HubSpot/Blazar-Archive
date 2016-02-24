package com.hubspot.orion.externalservice.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class SlackApiResponse {

  private final boolean ok;
  private final String error;

  @JsonCreator
  public SlackApiResponse(@JsonProperty("ok") boolean ok, @JsonProperty("error") String error) {
    this.ok = ok;
    this.error = error;
  }

  public boolean getOk() {
    return ok;
  }

  public String getError() {
    return error;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("ok", ok)
        .add("error", error)
        .toString();
  }

}
