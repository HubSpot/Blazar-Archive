package com.hubspot.blazar.externalservice.slack;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class SlackApiResponse {

  private final boolean ok;
  private final String error;
  private Optional<List<SlackChannel>> channels;

  @JsonCreator
  public SlackApiResponse(@JsonProperty("ok") boolean ok,
                          @JsonProperty("error") String error,
                          @JsonProperty("channels") Optional<List<SlackChannel>> channels) {
    this.ok = ok;
    this.error = error;
    this.channels = channels;
  }

  public boolean getOk() {
    return ok;
  }

  public String getError() {
    return error;
  }

  public Optional<List<SlackChannel>> getChannels() {
    return channels;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("ok", ok)
        .add("error", error)
        .toString();
  }

}
