package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

public class LogChunk {
  private final String data;
  private final long offset;
  private final Optional<Long> nextOffset;

  @JsonCreator
  public LogChunk(@JsonProperty("data") String data,
                  @JsonProperty("offset") long offset,
                  @JsonProperty("nextOffset") Optional<Long> nextOffset) {
    this.data = data;
    this.offset = offset;
    this.nextOffset = nextOffset;
  }

  public LogChunk withNextOffset(long nextOffset) {
    return new LogChunk(data, offset, Optional.of(nextOffset));
  }

  public String getData() {
    return data;
  }

  public long getOffset() {
    return offset;
  }

  public Optional<Long> getNextOffset() {
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
