package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class LogChunk {
  private final String data;
  private final long offset;

  @JsonCreator
  public LogChunk(@JsonProperty("data") String data, @JsonProperty("offset") long offset) {
    this.data = data;
    this.offset = offset;
  }

  public String getData() {
    return data;
  }

  public long getOffset() {
    return offset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LogChunk logChunk = (LogChunk) o;
    return Objects.equals(offset, logChunk.offset) && Objects.equals(data, logChunk.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, offset);
  }
}
