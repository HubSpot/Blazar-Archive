package com.hubspot.blazar.externalservice.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 "ok": true,
 "members": [
 {
 "id": "U023BECGF",
 "name": "bobby",
 "deleted": false,
 "color": "9f69e7",
 "profile": {
 "first_name": "Bobby",
 "last_name": "Tables",
 "real_name": "Bobby Tables",
 "email": "bobby@slack.com",
 "skype": "my-skype-name",
 "phone": "+1 (123) 456 7890",
 "image_24": "https:\/\/...",
 "image_32": "https:\/\/...",
 "image_48": "https:\/\/...",
 "image_72": "https:\/\/...",
 "image_192": "https:\/\/..."
 },
 "is_admin": true,
 "is_owner": true,
 "has_2fa": false,
 "has_files": true
 },
 ...
 ]
 }
 */

public class SlackUser {
  private final String id;
  private final String name;
  private final SlackUserProfile profile;

  @JsonCreator
  public SlackUser(@JsonProperty("id") String id,
                   @JsonProperty("name") String name,
                   @JsonProperty("profile") SlackUserProfile profile) {
    this.id = id;
    this.name = name;
    this.profile = profile;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public SlackUserProfile getProfile() {
    return profile;
  }
}

