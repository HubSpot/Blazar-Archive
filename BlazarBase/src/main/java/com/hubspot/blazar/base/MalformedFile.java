package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
