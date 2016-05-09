package org.kohsuke.github;

import java.io.IOException;

public class BlazarGHCompare extends GHCompare {

  static class Commit extends GHCompare.Commit {
    private final BlazarGHCompare.InnerCommit innerCommit;

    public Commit (BlazarGHCompare.InnerCommit innerCommit) {
      this.innerCommit = innerCommit;
      this.sha = innerCommit.sha;
    }

    @Override
    public GHCompare.InnerCommit getCommit() {
      return innerCommit;
    }

    @Override
    public String getSHA1() {
      return innerCommit.getSha();
    }
  }

  private static class InnerCommit extends  GHCompare.InnerCommit {
    private String url, sha, message;
    private GHCommit.User author;
    private GHCommit.User committer;
    private GHTree tree;

    public InnerCommit (BlazarGHCommit commit) throws IOException {
      this.sha = commit.getSHA1();
    }

    @Override
    public String getSha() {
      return sha;
    }

  }



  private Commit[] commits;

  public BlazarGHCompare(Commit[] commits){
    this.commits = commits;
  }

  @Override
  public Commit[] getCommits() {
    return this.commits;
  }

  public static Commit makeCommit(BlazarGHCommit commit) throws IOException {
    return new Commit(new InnerCommit(commit));
  }
}
