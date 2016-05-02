package org.kohsuke.github;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHBranch extends GHBranch {

  private final String name;
  private final String sha;
  private GHCommit commit;
  private GHRepository repository;

  @JsonCreator
  public BlazarGHBranch(@JsonProperty("name") String name,
                        @JsonProperty("sha") String sha) {
    this.name = name;
    this.sha = sha;
  }

  @Override
  public String getSHA1() {
    return commit.getSHA1();
  }

  @Override
  public String getName() {
    return name;
  }


  @Override
  public GHRepository getOwner() {
    return repository;
  }

  public void setRepository(GHRepository repository) throws IOException {
    this.repository = repository;
    this.commit = this.repository.getCommit(sha);
  }
}
