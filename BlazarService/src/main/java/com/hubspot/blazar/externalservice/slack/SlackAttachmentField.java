package com.hubspot.blazar.externalservice.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class SlackAttachmentField  {
  private final String title;
  private final String value;
  private final boolean shortValue;

  @JsonCreator
  public SlackAttachmentField(@JsonProperty("title") String title,
      @JsonProperty("value") String value,
      @JsonProperty("short") boolean shortValue) {
    this.title = title;
    this.value = value;
    this.shortValue = shortValue;
  }

  public String getTitle() {
    return title;
  }

  public String getValue() {
    return value;
  }

  @JsonProperty("short")
  public boolean isShortValue() {
    return shortValue;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("title", title)
        .add("value", value)
        .add("shortValue", shortValue)
        .toString();
  }
}
