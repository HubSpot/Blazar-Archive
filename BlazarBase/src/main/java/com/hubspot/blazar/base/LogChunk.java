package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LogChunk {
  private final String data;
  private final long offset;
  private final long nextOffset;

  public LogChunk(String data, long offset) {
    this(data, offset, offset + data.getBytes(StandardCharsets.UTF_8).length);
  }

  public LogChunk(byte[] data, long offset) {
    this(new String(data, StandardCharsets.UTF_8), offset, offset + data.length);
  }

  @JsonCreator
  public LogChunk(@JsonProperty("data") String data,
                  @JsonProperty("offset") long offset,
                  @JsonProperty("nextOffset") long nextOffset) {
    this.data = data;
    this.offset = offset;
    this.nextOffset = nextOffset;
  }

  public String getData() {
    return data;
  }

  public long getOffset() {
    return offset;
  }

  public long getNextOffset() {
    return nextOffset;
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
