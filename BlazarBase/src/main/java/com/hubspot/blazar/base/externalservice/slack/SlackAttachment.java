package com.hubspot.blazar.base.externalservice.slack;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class SlackAttachment {
  private static final Optional<String> ABSENT_STRING = Optional.absent();

  private final String fallback;
  private final Optional<String> color;
  private final Optional<String> pretext;

  // author
  private final Optional<String> authorName;
  private final Optional<String> authorLink;
  private final Optional<String> authorIcon;

  // title
  private final Optional<String> title;
  private final Optional<String> titleLink;

  private final Optional<String> text;

  private final List<SlackAttachmentField> fields;

  private final Optional<String> thumbUrl;

  @JsonCreator
  public SlackAttachment(
      @JsonProperty("fallback") String fallback,
      @JsonProperty("color") Optional<String> color,
      @JsonProperty("pretext") Optional<String> pretext,
      @JsonProperty("author_name") Optional<String> authorName,
      @JsonProperty("author_link") Optional<String> authorLink,
      @JsonProperty("author_icon") Optional<String> authorIcon,
      @JsonProperty("title") Optional<String> title,
      @JsonProperty("title_link") Optional<String> titleLink,
      @JsonProperty("text") Optional<String> text,
      @JsonProperty("fields") List<SlackAttachmentField> fields,
      @JsonProperty("thumb_url") Optional<String> thumbUrl) {

    this.fallback = fallback;
    this.color = Objects.firstNonNull(color, ABSENT_STRING);
    this.pretext = Objects.firstNonNull(pretext, ABSENT_STRING);
    this.authorName = Objects.firstNonNull(authorName, ABSENT_STRING);
    this.authorLink = Objects.firstNonNull(authorLink, ABSENT_STRING);
    this.authorIcon = Objects.firstNonNull(authorIcon, ABSENT_STRING);
    this.title = Objects.firstNonNull(title, ABSENT_STRING);
    this.titleLink = Objects.firstNonNull(titleLink, ABSENT_STRING);
    this.text = Objects.firstNonNull(text, ABSENT_STRING);
    this.fields = fields;
    this.thumbUrl = Objects.firstNonNull(thumbUrl, ABSENT_STRING);
  }

  public String getFallback() {
    return fallback;
  }

  public Optional<String> getColor() {
    return color;
  }

  public Optional<String> getPretext() {
    return pretext;
  }

  @JsonProperty("author_name")
  public Optional<String> getAuthorName() {
    return authorName;
  }

  @JsonProperty("author_link")
  public Optional<String> getAuthorLink() {
    return authorLink;
  }

  @JsonProperty("author_icon")
  public Optional<String> getAuthorIcon() {
    return authorIcon;
  }

  public Optional<String> getTitle() {
    return title;
  }

  @JsonProperty("title_link")
  public Optional<String> getTitleLink() {
    return titleLink;
  }

  public Optional<String> getText() {
    return text;
  }

  public List<SlackAttachmentField> getFields() {
    return fields;
  }

  @JsonProperty("thumb_url")
  public Optional<String> getThumbUrl() {
    return thumbUrl;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("fallback", fallback)
        .add("color", color)
        .add("pretext", pretext)
        .add("authorName", authorName)
        .add("authorLink", authorLink)
        .add("authorIcon", authorIcon)
        .add("title", title)
        .add("titleLink", titleLink)
        .add("text", text)
        .add("fields", fields)
        .add("thumbUrl", thumbUrl)
        .toString();
  }
}
