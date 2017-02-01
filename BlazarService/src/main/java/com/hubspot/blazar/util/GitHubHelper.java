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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.net.UrlEscapers;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.User;

@Singleton
public class GitHubHelper {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubHelper.class);
  private static final String MAINTENANCE_MESSAGE = "GitHub Enterprise is currently down for maintenance";
  private static final String GIT_HUB_MAINTENANCE_LOG_MESSAGE = "GitHub Enterprise is down for maintenance. Can not proceed with the build.";

  private final Map<String, GitHub> gitHubByHost;
  private final ObjectMapper mapper;
  private final YAMLFactory yamlFactory;

  @Inject
  public GitHubHelper(Map<String, GitHub> gitHubByHost, ObjectMapper mapper, YAMLFactory yamlFactory) {
    this.gitHubByHost = gitHubByHost;
    this.mapper = mapper;
    this.yamlFactory = yamlFactory;
  }

  public Optional<String> shaFor(GHRepository repository, GitInfo gitInfo) {
    final GHBranch branch;
    try {
        branch = repository.getBranches().get(gitInfo.getBranch());
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.warn(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception trying to find sha for {}", gitInfo, e);
      throw new RuntimeException(e);
    }

    if (branch == null) {
      return Optional.absent();
    } else {
      return Optional.of(branch.getSHA1());
    }
  }

  public CommitInfo commitInfoFor(GHRepository repository, Commit current, Optional<Commit> previous) {
    final List<Commit> newCommits;
    boolean truncated = false;

    try {
      if (previous.isPresent()) {
        newCommits = new ArrayList<>();

        GHCompare compare = repository.getCompare(previous.get().getId(), current.getId());
        List<GHCompare.Commit> commits = Arrays.asList(compare.getCommits());


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

    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.warn(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while processing diff commits on {} between {} and {}", repository, current, previous);
      throw new RuntimeException(e);
    }

    return new CommitInfo(current, previous, newCommits, truncated);
  }

  public Optional<BuildConfig> configFor(String path, GitInfo gitInfo) {
    return configFor(path, repositoryFor(gitInfo), gitInfo);
  }

  public Optional<BuildConfig> configFor(String path, GHRepository repository, GitInfo gitInfo) {
    try {
      String config = contentsFor(path, repository, gitInfo);
      return Optional.of(mapper.readValue(yamlFactory.createParser(config), BuildConfig.class));
    } catch (FileNotFoundException e) {
      String message = String.format("No repository found for %s", gitInfo.getFullRepositoryName());
      throw new NonRetryableBuildException(message, e);
    } catch (JsonProcessingException e) {
      String message = String.format("Invalid config found for path %s in repo %s@%s, failing build", path, gitInfo.getFullRepositoryName(), gitInfo.getBranch());
      throw new NonRetryableBuildException(message, e);
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)){
        LOG.error(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while trying to read config {} in repo {} from GitHub", path, gitInfo, e);
      throw new RuntimeException(e);
    }
  }

  public String contentsFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    GHContent content = repository.getFileContent(file, gitInfo.getBranch());
    return content.getContent();
  }

  public GHTree treeFor(GHRepository repository, GitInfo gitInfo) {
    String escapedBranch = UrlEscapers.urlPathSegmentEscaper().escape(gitInfo.getBranch());
    try {
      return repository.getTreeRecursive(escapedBranch, 1);
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.error(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while trying to get repository tree for {} from GitHub", gitInfo, e);
      throw new RuntimeException(e);
    }
  }

  public GHRepository repositoryFor(GitInfo gitInfo) {
    try {
      return gitHubFor(gitInfo).getRepository(gitInfo.getFullRepositoryName());
    } catch (FileNotFoundException e) {
      LOG.warn("Repository {} not found", gitInfo, e);
      throw new NonRetryableBuildException("Repository not found", e);
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.error(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while trying to get repository data for {} from GitHub", gitInfo, e);
      throw new RuntimeException(e);
    }
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

  public Commit toCommitFromRepoAndSha(GHRepository repository, String sha) {
    try {
      return toCommit(repository.getCommit(sha));
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.warn(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while processing diff commits");
      throw new RuntimeException(e);
    }
  }


  public Commit toCommit(GHCommit commit) {
    Commit.Builder builder = Commit.newBuilder()
        .setId(commit.getSHA1())
        .setMessage(commit.getCommitShortInfo().getMessage())
        .setTimestamp(String.valueOf(commit.getCommitShortInfo().getCommitter().getDate().getTime()))
        .setUrl(commit.getOwner().getHtmlUrl() + "/commit/" + commit.getSHA1())
        .setAuthor(toAuthor(commit))
        .setCommitter(toCommitter(commit));

    final List<GHCommit.File> commitFiles;
    try {
       commitFiles = commit.getFiles();
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.warn(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while reading list of changed files in commit {}", commit, e);
      throw new RuntimeException(e);
    }

    for (GHCommit.File file : commitFiles) {
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

  private static User toAuthor(GHCommit commit) {
    User.Builder builder = toUser(commit.getCommitShortInfo().getAuthor());
    try {
      if (commit.getAuthor() != null && commit.getAuthor().getLogin() != null) {
        builder.setUsername(commit.getAuthor().getLogin());
      }
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.warn(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while getting author info for {}", commit, e);
      throw new RuntimeException(e);
    }

    return builder.build();
  }

  private static User toCommitter(GHCommit commit) {
    User.Builder builder = toUser(commit.getCommitShortInfo().getCommitter());
    try {
      if (commit.getCommitter() != null && commit.getCommitter().getLogin() != null) {
        builder.setUsername(commit.getCommitter().getLogin());
      }
    } catch (IOException e) {
      if (githubIsInMaintenanceMode(e)) {
        LOG.warn(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
        throw new NonRetryableBuildException(GIT_HUB_MAINTENANCE_LOG_MESSAGE, e);
      }
      LOG.warn("Caught exception while getting committer info for {}", commit, e);
      throw new RuntimeException(e);
    }

    return builder.build();
  }

  private static User.Builder toUser(GitUser user) {
    return User.newBuilder().setName(user.getName()).setEmail(user.getEmail());

  }

  private static boolean githubIsInMaintenanceMode(IOException e) {
    return e.getMessage().contains(MAINTENANCE_MESSAGE);
  }
}
