package com.hubspot.blazar.externalservice.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class SlackWebhook {

  private final String token;
  private final String teamId;
  private final String teamDomain;
  private final String channelId;
  private final String channelName;
  private final String timestamp;
  private final String userId;
  private final String userName;
  private final String text;
  private final String triggerWord;

  @JsonCreator
  public SlackWebhook(
      @JsonProperty("token") String token,
      @JsonProperty("team_id") String teamId,
      @JsonProperty("team_domain") String teamDomain,
      @JsonProperty("channel_id") String channelId,
      @JsonProperty("channel_name") String channelName,
      @JsonProperty("timestamp") String timestamp,
      @JsonProperty("user_id") String userId,
      @JsonProperty("user_name") String userName,
      @JsonProperty("text") String text,
      @JsonProperty("trigger_word") String triggerWord) {

    this.token = token;
    this.teamId = teamId;
    this.teamDomain = teamDomain;
    this.channelId = channelId;
    this.channelName = channelName;
    this.timestamp = timestamp;
    this.userId = userId;
    this.userName = userName;
    this.text = text;
    this.triggerWord = triggerWord;
  }

  public String getToken() {
    return token;
  }

  public String getTeamId() {
    return teamId;
  }

  public String getTeamDomain() {
    return teamDomain;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getChannelName() {
    return channelName;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getText() {
    return text;
  }

  public String getTriggerWord() {
    return triggerWord;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("token", token)
        .add("teamId", teamId)
        .add("teamDomain", teamDomain)
        .add("channelId", channelId)
        .add("channelName", channelName)
        .add("timestamp", timestamp)
        .add("userId", userId)
        .add("userName", userName)
        .add("text", text)
        .add("triggerWord", triggerWord)
        .toString();
  }

}
