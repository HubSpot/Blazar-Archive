package org.kohsuke.github;

import org.kohsuke.github.GHCompare;

public class BlazarGHCompare extends GHCompare {

  private Commit[] commits;

  public BlazarGHCompare(Commit[] commits){
    this.commits = commits;
  }

  @Override
  public Commit[] getCommits() {
    return this.commits;
  }
}
