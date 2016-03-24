package com.hubspot.blazar.externalservice.slack;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class SlackMessage {
  private final Optional<String> token;
  private final String channel;
  private final String text;
  private final List<SlackAttachment> attachments;
  private final Optional<String> username;
  private final Boolean as_user;
  private final Optional<String> icon_url;
  private final Optional<String> icon_emoji;

  /**
   * Use the builder
   */
  @JsonCreator
  public SlackMessage(
      @JsonProperty("token") Optional<String> token,
      @JsonProperty("channel") String channel,
      @JsonProperty("text") String text,
      @JsonProperty("attachments") List<SlackAttachment> attachments,
      @JsonProperty("username") Optional<String> username,
      @JsonProperty("as_user") Boolean as_user,
      @JsonProperty("icon_url") Optional<String> icon_url,
      @JsonProperty("icon_emoji") Optional<String> icon_emoji) {
    this.token = token;
    this.channel = channel;
    this.text = text;
    this.attachments = attachments;
    this.username = username;
    this.as_user = as_user;
    this.icon_url = icon_url;
    this.icon_emoji = icon_emoji;
  }

  public Optional<String> getToken() {
    return token;
  }

  public String getChannel() {
    return channel;
  }

  public String getText() {
    return text;
  }

  public List<SlackAttachment> getAttachments() {
    return attachments;
  }

  public Optional<String> getUsername() {
    return username;
  }

  public Boolean getAs_user() {
    return as_user;
  }

  public Optional<String> getIcon_url() {
    return icon_url;
  }

  public Optional<String> getIcon_emoji() {
    return icon_emoji;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("text", text)
        .add("username", username)
        .add("channel", channel)
        .add("as_user", as_user)
        .add("attachments", attachments)
        .toString();
  }
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    Optional<String> token;
    String channel;
    String text;
    List<SlackAttachment> attachments;
    Optional<String> username;
    Boolean as_user;
    Optional<String> icon_url;
    Optional<String> icon_emoji;

    public Builder () {
      this.token = Optional.absent();
      this.channel = "";
      this.text = "";
      this.attachments = new ArrayList<>();
      this.username = Optional.absent();
      this.as_user = true;
      this.icon_url = Optional.absent();
      this.icon_emoji = Optional.absent();
    }

    public SlackMessage build() {
      if (channel.isEmpty() || text.isEmpty()) {
        throw new RuntimeException("Cannot send empty message or send to empty channel");
      }
      return new SlackMessage(token, channel, text, attachments, username, as_user, icon_url, icon_emoji);
    }

    public Optional<String> getToken() {
      return token;
    }

    public String getChannel() {
      return channel;
    }

    public String getText() {
      return text;
    }

    public List<SlackAttachment> getAttachments() {
      return attachments;
    }

    public Optional<String> getUsername() {
      return username;
    }

    public Boolean getAs_user() {
      return as_user;
    }

    public Optional<String> getIcon_url() {
      return icon_url;
    }

    public Optional<String> getIcon_emoji() {
      return icon_emoji;
    }

    public void setToken(Optional<String> token) {
      this.token = token;
    }

    public void setChannel(String channel) {
      this.channel = channel;
    }

    public void setText(String text) {
      this.text = text;
    }

    public void setAttachments(List<SlackAttachment> attachments) {
      this.attachments = attachments;
    }

    public void setUsername(Optional<String> username) {
      this.username = username;
    }

    public void setAs_user(Boolean as_user) {
      this.as_user = as_user;
    }

    public void setIcon_url(Optional<String> icon_url) {
      this.icon_url = icon_url;
    }

    public void setIcon_emoji(Optional<String> icon_emoji) {
      this.icon_emoji = icon_emoji;
    }
  }
}
