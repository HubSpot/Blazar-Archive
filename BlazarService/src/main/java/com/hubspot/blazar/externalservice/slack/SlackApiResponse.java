package com.hubspot.blazar.externalservice.slack;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackApiResponse {

  private final boolean ok;
  private final Optional<String> error;
  private final Optional<List<SlackUser>> users;
  private Optional<List<SlackChannel>> channels;

  @JsonCreator
  public SlackApiResponse(@JsonProperty("ok") boolean ok,
                          @JsonProperty("error") Optional<String> error,
                          @JsonProperty("channels") Optional<List<SlackChannel>> channels,
                          @JsonProperty("users") Optional<List<SlackUser>> users) {
    this.ok = ok;
    this.error = error;
    this.channels = channels;
    this.users = users;
  }

  public boolean getOk() {
    return ok;
  }

  public Optional<String> getError() {
    return error;
  }

  public Optional<List<SlackChannel>> getChannels() {
    return channels;
  }

  public Optional<List<SlackUser>> getUsers() {
    return users;
  }

  @Override
  public String toString() {
    Objects.ToStringHelper h = Objects.toStringHelper(this);
    h.add("ok", ok);
    if (error.isPresent()) {
      h.add("error", error.get());
    } else {
      h.add("error", Optional.absent().toString());
    }
    return h.toString();
  }

}
