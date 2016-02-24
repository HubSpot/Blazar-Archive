package com.hubspot.blazar.base.externalservice.slack;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class SlackMessage {
  private final Optional<String> token;
  private final Optional<String> text;
  private final Optional<String> username;
  private final Optional<String> iconEmoji;
  private final String channel;
  private final List<SlackAttachment> attachments;

  @JsonCreator
  public SlackMessage(
      @JsonProperty("token") Optional<String> token,
      @JsonProperty("text") Optional<String> text,
      @JsonProperty("username") Optional<String> username,
      @JsonProperty("icon_emoji") Optional<String> iconEmoji,
      @JsonProperty("channel") String channel,
      @JsonProperty("attachments") List<SlackAttachment> attachments) {

    this.token = token == null ? Optional.<String>absent() : token;
    this.text = text == null ? Optional.<String>absent() : text;
    this.username = username == null ? Optional.<String>absent() : username;
    this.iconEmoji = iconEmoji;
    this.channel = channel;
    this.attachments = attachments;
  }

  public Optional<String> getToken() {
    return token;
  }

  public Optional<String> getText() {
    return text;
  }

  public Optional<String> getUsername() {
    return username;
  }

  @JsonProperty("icon_emoji")
  public Optional<String> getIconEmoji() {
    return iconEmoji;
  }

  public String getChannel() {
    return channel;
  }

  public List<SlackAttachment> getAttachments() {
    return attachments;
  }

  public SlackMessage cloneWithToken(String token) {
    return new SlackMessage(Optional.of(token), text, username, iconEmoji, channel, ImmutableList.copyOf(attachments));
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("text", text)
        .add("username", username)
        .add("iconEmoji", iconEmoji)
        .add("channel", channel)
        .add("attachments", attachments)
        .toString();
  }
}
