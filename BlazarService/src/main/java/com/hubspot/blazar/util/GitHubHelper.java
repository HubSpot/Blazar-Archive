package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.net.UrlEscapers;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.github.GitHubErrorResponse;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.User;

@Singleton
public class GitHubHelper {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubHelper.class);

  private final Map<String, GitHub> gitHubByHost;
  private final ObjectMapper mapper;
  private final YAMLFactory yamlFactory;

  @Inject
  public GitHubHelper(Map<String, GitHub> gitHubByHost, ObjectMapper mapper, YAMLFactory yamlFactory) {
    this.gitHubByHost = gitHubByHost;
    this.mapper = mapper;
    this.yamlFactory = yamlFactory;
  }

  public Optional<String> shaFor(GHRepository repository, GitInfo gitInfo) throws IOException {
    GHBranch branch = repository.getBranches().get(gitInfo.getBranch());
    if (branch == null) {
      return Optional.absent();
    } else {
      return Optional.of(branch.getSHA1());
    }
  }

  public CommitInfo commitInfoFor(GHRepository repository, Commit current, Optional<Commit> previous) throws IOException {
    final List<Commit> newCommits;
    boolean truncated = false;
    if (previous.isPresent()) {
      newCommits = new ArrayList<>();

      List<GHCompare.Commit> commits = Collections.emptyList();
      try {
        GHCompare compare = repository.getCompare(previous.get().getId(), current.getId());
        commits = Arrays.asList(compare.getCommits());
      } catch (FileNotFoundException e) {
        LOG.warn("Error generating compare from sha {} to sha {}", previous.get().getId(), current.getId(), e);
      }

      if (commits.size() > 10) {
        commits = commits.subList(commits.size() - 10, commits.size());
        truncated = true;
      }

      for (GHCompare.Commit newCommit : commits) {
        newCommits.add(toCommit(repository.getCommit(newCommit.getSHA1())));
      }
    } else {
      newCommits = Collections.emptyList();
      truncated = true;
    }

    return new CommitInfo(current, previous, newCommits, truncated);
  }

  public Optional<BuildConfig> getCurrentConfigOnBranch(String path, GitInfo gitInfo) throws IOException {
    return configFor(path, repositoryFor(gitInfo), gitInfo);
  }

  public Optional<BuildConfig> configAtSha(String path, GitInfo gitInfo, String sha) throws IOException {
    GHContent fileContent = repositoryFor(gitInfo).getFileContent(path, sha);
    return Optional.of(mapper.readValue(yamlFactory.createParser(fileContent.getContent()), BuildConfig.class));
  }

  public Optional<BuildConfig> configFor(String path, GHRepository repository, GitInfo gitInfo) throws IOException {
    final String config;
    try {
      config = contentsFor(path, repository, gitInfo);
    } catch (FileNotFoundException e) {
      return Optional.absent();
    }

    return Optional.of(mapper.readValue(yamlFactory.createParser(config), BuildConfig.class));
  }

  public String contentsFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    GHContent content = repository.getFileContent(file, gitInfo.getBranch());
    return content.getContent();
  }

  public GHTree treeFor(GHRepository repository, GitInfo gitInfo) throws IOException {
    String escapedBranch = UrlEscapers.urlPathSegmentEscaper().escape(gitInfo.getBranch());
    return repository.getTreeRecursive(escapedBranch, 1);
  }

  public GHRepository repositoryFor(GitInfo gitInfo) throws IOException {
    return gitHubFor(gitInfo).getRepository(gitInfo.getFullRepositoryName());
  }

  public GitHub gitHubFor(GitInfo gitInfo) {
    String host = gitInfo.getHost();

    return Preconditions.checkNotNull(gitHubByHost.get(host), "No GitHub found for host " + host);
  }

  public Set<String> affectedPaths(CommitInfo commitInfo) {
    Set<String> affectedPaths = new HashSet<>();
    for (Commit commit : commitInfo.getNewCommits()) {
      affectedPaths.addAll(commit.getAddedList());
      affectedPaths.addAll(commit.getModifiedList());
      affectedPaths.addAll(commit.getRemovedList());
    }

    return affectedPaths;
  }

  public Commit toCommit(GHCommit commit) throws IOException {
    Commit.Builder builder = Commit.newBuilder()
        .setId(commit.getSHA1())
        .setMessage(commit.getCommitShortInfo().getMessage())
        .setTimestamp(String.valueOf(commit.getCommitShortInfo().getCommitter().getDate().getTime()))
        .setUrl(commit.getOwner().getHtmlUrl() + "/commit/" + commit.getSHA1())
        .setAuthor(toAuthor(commit))
        .setCommitter(toCommitter(commit));

    for (GHCommit.File file : commit.getFiles()) {
      switch (file.getStatus()) {
        case "added":
          builder.addAdded(file.getFileName());
          break;
        case "modified":
        case "changed":
        case "renamed":
          builder.addModified(file.getFileName());
          break;
        case "removed":
          builder.addRemoved(file.getFileName());
          break;
        default:
          throw new IllegalArgumentException("Unrecognized file status: " + file.getStatus());
      }
    }

    return builder.build();
  }


  public Optional<GitHubErrorResponse> extractErrorResponseFromException(Throwable t) {
    try {
      return Optional.of(mapper.readValue(t.getMessage(), GitHubErrorResponse.class));
    } catch (IOException e){
      return Optional.absent();
    }
  }

  private static User toAuthor(GHCommit commit) throws IOException {
    User.Builder builder = toUser(commit.getCommitShortInfo().getAuthor());

    if (commit.getAuthor() != null && commit.getAuthor().getLogin() != null) {
      builder.setUsername(commit.getAuthor().getLogin());
    }

    return builder.build();
  }

  private static User toCommitter(GHCommit commit) throws IOException {
    User.Builder builder = toUser(commit.getCommitShortInfo().getCommitter());

    if (commit.getCommitter() != null && commit.getCommitter().getLogin() != null) {
      builder.setUsername(commit.getCommitter().getLogin());
    }

    return builder.build();
  }

  private static User.Builder toUser(GitUser user) {
    return User.newBuilder().setName(user.getName()).setEmail(user.getEmail());

  }
}
