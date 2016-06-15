package org.kohsuke.github;

import java.util.List;

import com.google.common.base.Optional;

public class BlazarGHChange {
  private final BlazarGHCommit commit;
  private final List<BlazarGHTreeEntry> entries;
  private String branch;
  private Optional<String> oldCommitSha1;
  private Optional<List<BlazarGHTreeEntry>> oldEntries;

  /**
   * @param commit Commit to add
   * @param entries Tree entries to add
   * @param branch branch to change "HEAD" of
   */
  public BlazarGHChange(BlazarGHCommit commit, List<BlazarGHTreeEntry> entries, String branch) {
    this.commit = commit;
    this.entries = entries;
    this.branch = branch;
    // previous values so we can restore them
    this.oldEntries = Optional.absent();
    this.oldCommitSha1 = Optional.absent();
  }

  public BlazarGHCommit getCommit() {
    return commit;
  }

  public List<BlazarGHTreeEntry> getEntries() {
    return entries;
  }

  public String getBranch() {
    return branch;
  }

  public Optional<List<BlazarGHTreeEntry>> getOldEntries() {
    return oldEntries;
  }

  public Optional<String> getOldCommitSha1() {
    return oldCommitSha1;
  }

  public void setOldEntries(Optional<List<BlazarGHTreeEntry>> oldEntries) {
    this.oldEntries = oldEntries;
  }

  public void setOldCommitSha1(Optional<String> oldCommitSha1) {
    this.oldCommitSha1 = oldCommitSha1;
  }
}
