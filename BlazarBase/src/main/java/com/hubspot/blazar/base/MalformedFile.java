package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class MalformedFile {
  private final int branchId;
  private final String type;
  private final String path;
  private final String details;

  @JsonCreator
  public MalformedFile(@JsonProperty("branchId") int branchId,
                       @JsonProperty("type") String type,
                       @JsonProperty("path") String path,
                       @JsonProperty("details") String details) {
    this.branchId = branchId;
    this.type = type;
    this.path = path;
    this.details = details;
  }

  public int getBranchId() {
    return branchId;
  }

  public String getType() {
    return type;
  }

  public String getPath() {
    return path;
  }

  public String getDetails() {
    return details;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MalformedFile that = (MalformedFile) o;
    return branchId == that.branchId &&
        Objects.equal(type, that.type) &&
        Objects.equal(path, that.path) &&
        Objects.equal(details, that.details);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branchId, type, path, details);
  }
}
