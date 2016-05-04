package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlazarGHRepository extends GHRepository {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarGHRepository.class);
  private final String name;
  private final String fullName;
  private final Map<String, ? extends GHBranch> branches;
  private final BlazarGHUser owner;
  private final List<BlazarGHCommit> commits;
  private final BlazarGHTree tree;
  private String host;

  @JsonCreator
  public BlazarGHRepository(@JsonProperty("name") String name,
                            @JsonProperty("commits") List<BlazarGHCommit> commits,
                            @JsonProperty("branches") Map<String, BlazarGHBranch> branches,
                            @JsonProperty("tree") BlazarGHTree tree,
                            @JsonProperty("owner") BlazarGHUser owner) throws IOException {
    this.name = name;
    this.owner = owner;
    this.commits = commits;
    for (BlazarGHBranch  b : branches.values()) {
      b.setRepository(this);
    }
    for (BlazarGHCommit c : commits) {
      c.setRepository(this);
    }
    this.branches = branches;
    this.tree = tree;
    this.fullName = String.format("%s/%s", owner.getLogin(), name);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getFullName() {
    return this.fullName;
  }

  @Override
  public GHTree getTreeRecursive(String gitBranchName, int recursive) throws IOException {
    // So far in Blazar we only call this with branch name, afaict it can be any gitRef + int
    if (branches.keySet().contains(gitBranchName)) {
      String sha1 = branches.get(gitBranchName).getSHA1();
      // find the point in the tree that has the sha we're looking for
      // tree is really a misnomer... this is a list. :shrug: that's the ghe api for you
      for (GHTreeEntry entry : tree.getTree()) {
        if (entry.getSha().equals(sha1)) {
          int index = tree.getTree().indexOf(entry);
          return new BlazarGHTree(sha1, tree.getSubclassTree().subList(index, tree.getTree().size()));
        }
      }
      throw new IllegalArgumentException(String.format("The sha %s for branch %s points to a sha which doesn't exist in our tree", sha1, gitBranchName));
    }
    throw new IllegalArgumentException(String.format("Branch %s doesn't exist", gitBranchName));

  }

  @Override
  public Map<String, GHBranch> getBranches() throws IOException {
    return (Map<String, GHBranch>) this.branches;
  }

  @Override
  public GHCommit getCommit(String sha1) throws IOException {
    for (BlazarGHCommit c : commits) {
      if (c.getSHA1().equals(sha1)) {
        return c;
      }
    }
    throw new IllegalArgumentException("No commit with that sha in this repo");
  }

  @Override
  public GHCompare getCompare(String id1, String id2) throws IOException {
    return new BlazarGHCompare(new GHCompare.Commit[0]);
  }

  @Override
  public URL getHtmlUrl() {
    try {
      return new URL("https", this.host, String.format("/%s/%s", this.owner.getLogin(), this.name));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public GHCommitStatus createCommitStatus(String sha1, GHCommitState state, String targetUrl, String description) throws IOException {
    return createCommitStatus(sha1, state, targetUrl, description, "Blazar");
  }

  @Override
  public GHCommitStatus createCommitStatus(String sha1, GHCommitState state, String targetUrl, String description, String context) throws IOException {
    LOG.info("Faking commit status creation {sha={} state={} message='{}' context={}}", sha1.substring(0, 8), state.toString(), description, context);
    return null;
  }

  @Override
  public GHContent getFileContent(String path, String ref) throws IOException {
    if (branches.containsKey(ref)) {
      BlazarGHTree tree = (BlazarGHTree) this.getTreeRecursive(ref, 1);
      for (BlazarGHTreeEntry entry : tree.getSubclassTree()) {
        if (entry.getPath().equals(path)) {
          return entry.getContent();
        }
      }
      throw new FileNotFoundException(String.format("No such file %s", path));
    }

    for (BlazarGHTreeEntry entry : tree.getSubclassTree()) {
      if (entry.getSha().equals(ref)) {
        return entry.getContent();
      }
    }
    throw new IllegalArgumentException(String.format("No such ref %s", ref));
  }

  public void setHost(String host) {
    this.host = host;
  }

}
