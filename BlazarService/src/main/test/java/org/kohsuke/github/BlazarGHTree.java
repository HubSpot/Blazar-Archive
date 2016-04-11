package org.kohsuke.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

public class BlazarGHTree extends GHTree {
  private String sha;
  private final List<BlazarGHTreeEntry> tree;
  private final boolean truncated;

  @JsonCreator
  public BlazarGHTree(@JsonProperty("sha") String sha,
                      @JsonProperty("tree") List<BlazarGHTreeEntry> tree) {
    this.sha = sha;
    this.tree = tree;
    this.truncated = false;
  }

  @Override
  public List<GHTreeEntry> getTree() {
    return ImmutableList.<GHTreeEntry>copyOf(this.tree);
  }

  public List<BlazarGHTreeEntry> getSubclassTree() {
    return this.tree;
  }

  @Override
  public String getSha() {
    return sha;
  }

  @Override
  public boolean isTruncated() {
    return this.truncated;
  }
}

