package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class BlazarGHRepository extends GHRepository {
  private final int id;
  private static final Logger LOG = LoggerFactory.getLogger(BlazarGHRepository.class);
  private final String name;
  private final String fullName;
  private Map<String, ? extends GHBranch> branches;
  private BlazarGHUser owner;
  private List<BlazarGHCommit> commits;
  private final BlazarGHTree tree;
  private String host;
  private List<BlazarGHChange> history;

  @JsonCreator
  public BlazarGHRepository(@JsonProperty("name") String name,
                            @JsonProperty("id") int id,
                            @JsonProperty("commits") List<BlazarGHCommit> commits,
                            @JsonProperty("branches") Map<String, BlazarGHBranch> branches,
                            @JsonProperty("tree") BlazarGHTree tree,
                            @JsonProperty("owner") BlazarGHUser owner) throws IOException {
    this.id = id;
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
    this.history = new ArrayList<>();
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getFullName() {
    return fullName;
  }

  @Override
  public String getOwnerName() {
    return owner.getLogin();
  }

  @Override
  public BlazarGHUser getOwner() {
    return owner;
  }

  public void setOwner(BlazarGHUser user) {
    owner = user;
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
    List<String> shas = new ArrayList<>();
    for (BlazarGHCommit commit : commits) {
      shas.add(commit.getSHA1());
    }
    // because the "newer" sha (id2) will be first in the shas list
    // we flip the indicies in the sublist call, to ensure the sublist isn't backwards
    int index1 = shas.indexOf(id1);
    int index2 = shas.indexOf(id2);
    List<BlazarGHCommit> compareListWrongType = commits.subList(index2, index1);
    List<BlazarGHCompare.Commit> compareList = new ArrayList<>();
    for (BlazarGHCommit commit : compareListWrongType) {
      compareList.add(BlazarGHCompare.makeCommit(commit));
    }
    BlazarGHCompare.Commit commitArray[] = new BlazarGHCompare.Commit[compareList.size()];
    return new BlazarGHCompare(compareList.toArray(commitArray));
  }

  @Override
  public URL getHtmlUrl() {
    try {
      return new URL("https", host, String.format("/%s/%s", owner.getLogin(), name));
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
      BlazarGHTree tree = (BlazarGHTree) getTreeRecursive(ref, 1);
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", fullName).toString();
  }

  public void applyChange(BlazarGHChange change) throws IOException {
    history.add(0, change);
    commits.add(0, change.getCommit());
    commits.get(0).setRepository(this);
    change.setOldCommitSha1(Optional.of(tree.getSha()));
    change.setOldEntries(Optional.<List<BlazarGHTreeEntry>>of(ImmutableList.copyOf(tree.getSubclassTree())));

    // point branch HEAD to new commit
    Map<String, BlazarGHBranch> newBranches = new HashMap<>();
    newBranches.putAll((Map<String, BlazarGHBranch>) branches);
    newBranches.put(change.getBranch(), new BlazarGHBranch(change.getBranch(), change.getCommit().getSHA1()));
    branches = newBranches;

    // mutate tree
    List<BlazarGHTreeEntry> newEntries = new ArrayList<>();
    newEntries.addAll(change.getEntries());
    newEntries.addAll(tree.getSubclassTree());
    newBranches.get(change.getBranch()).setRepository(this);
    tree.set(change.getCommit().getSHA1(), newEntries);
  }

  public void revertLastChange() throws IOException {
    if (history.size() <= 0)  {
      throw new UnsupportedOperationException("Cannot pop commit from empty history");
    }
    BlazarGHChange change = history.remove(0);
    if (!change.getOldEntries().isPresent() || !change.getOldCommitSha1().isPresent()) {
      throw new IllegalStateException("Change found in repo history with no old data to restore to");
    }
    commits.remove(0);
    tree.set(change.getOldCommitSha1().get(), change.getOldEntries().get());

    // change head back
    Map<String, BlazarGHBranch> newBranches = new HashMap<>();
    newBranches.putAll((Map<String, BlazarGHBranch>) branches);
    newBranches.put(change.getBranch(), new BlazarGHBranch(change.getBranch(), change.getOldCommitSha1().get()));
    newBranches.get(change.getBranch()).setRepository(this);
    branches = newBranches;
  }
}
