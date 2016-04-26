package org.kohsuke.github;

import org.kohsuke.github.GHCommit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHCommitFile extends GHCommit.File {
  private final String fileName;
  private final Status status;

  public enum Status {  // not used by the API but protects us against spelling errors when writing the test json
    added,
    modified,
    changed,
    renamed,
    removed,
  }


  @JsonCreator
  public BlazarGHCommitFile(@JsonProperty("fileName") String fileName,  // full path in repository
                            @JsonProperty("status") Status status){
    this.fileName = fileName;
    this.status = status;
  }

  @Override
  public String getFileName() {
    return this.fileName;
  }

  @Override
  public String getStatus() {
    return String.valueOf(this.status);
  }
}
