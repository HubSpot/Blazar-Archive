package org.kohsuke.github;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHCommit extends GHCommit {


  private final String sha1;
  private List<? extends GHCommit.File> files;
  private final BlazarGHCommitShortInfo commitShortInfo;
  private GHRepository repository;

  @JsonCreator
  public BlazarGHCommit(@JsonProperty("sha1") String sha1,
                        @JsonProperty("files") List<BlazarGHCommitFile> files,
                        @JsonProperty("commitShortInfo") BlazarGHCommitShortInfo commitShortInfo) {

    this.sha1 = sha1;
    this.files = files;
    this.commitShortInfo = commitShortInfo;
  }

  @Override
  public String getSHA1() {
    return this.sha1;
  }

  @Override
  public ShortInfo getCommitShortInfo() {
    return this.commitShortInfo;
  }

  @Override
  public List<GHCommit.File> getFiles() {
    return (List<File>) this.files;
  }

  @Override
  public GHUser getCommitter() throws IOException {
    return BlazarGitUser.toGHUser(this.commitShortInfo.getCommitter());
  }

  @Override
  public GHUser getAuthor() throws IOException {
    return BlazarGitUser.toGHUser(this.commitShortInfo.getAuthor());
  }

  @Override
  public GHRepository getOwner() {
    return this.repository;
  }

  public void setRepository(GHRepository ghRepository) {
    this.repository = ghRepository;
  }
}
