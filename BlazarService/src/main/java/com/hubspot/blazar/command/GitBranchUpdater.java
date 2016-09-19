package com.hubspot.blazar.command;

import java.io.IOException;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.util.GitHubHelper;

public class GitBranchUpdater implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(GitBranchUpdater.class);

  private GitHubHelper gitHubHelper;
  private final BlazarConfiguration configuration;
  private BranchService branchService;
  private GitInfo oldBranch;

  public GitBranchUpdater(GitInfo oldBranch,
                          GitHubHelper gitHubHelper,
                          BlazarConfiguration configuration,
                          BranchService branchService) {
    this.oldBranch = oldBranch;
    this.gitHubHelper = gitHubHelper;
    this.configuration = configuration;
    this.branchService = branchService;
  }

  @Override
  public void run() {
    if (!configuration.getGitHubConfiguration().containsKey(oldBranch.getHost())) {
      LOG.warn("No git host configured for {}/{}/{}#{} marking as inactive", oldBranch.getHost(), oldBranch.getOrganization(), oldBranch.getRepository(), oldBranch.getBranch());
      branchService.delete(oldBranch);
      return;
    }

    GitHubConfiguration githubConfiguration = configuration.getGitHubConfiguration().get(oldBranch.getHost());
    GHRepository ghRepository;
    try {
      ghRepository = gitHubHelper.repositoryFor(oldBranch);
    } catch (IOException e) {
      LOG.error("Caught exception while trying to find {} in github", oldBranch, e);
      return;
    }

    String oldRepoOrg = oldBranch.getOrganization();
    String oldRepoName = oldBranch.getRepository();
    String newRepoName = ghRepository.getName();
    String newRepoOrg = ghRepository.getOwnerName();

    // Whether we listen & process updates for repos in this org
    boolean orgIsConfigured = githubConfiguration.getOrganizations().contains(newRepoOrg);
    boolean orgNameChanged = !oldRepoOrg.equals(newRepoOrg);
    boolean repoNameChanged = !oldRepoName.equals(newRepoName);

    // Only update a branch if it has changed.
    if (orgNameChanged || repoNameChanged || orgIsConfigured != oldBranch.isActive()) {
      GitInfo updatedBranch = updateBranch(oldBranch, newRepoOrg, newRepoName, orgIsConfigured);
      // this log is in the format ORG/REPO#BRANCH (ACTIVE)
      String oldRepoLogString = String.format("%s/%s#%s (%s)", oldRepoOrg, oldRepoName, oldBranch.getBranch(), oldBranch.isActive());
      String newRepoLogString = String.format("%s/%s#%s (%s)", newRepoOrg, newRepoName, oldBranch.getBranch(), orgIsConfigured);
      LOG.info("Branch {} has changed updating to {}", oldRepoLogString, newRepoLogString);
      branchService.upsert(updatedBranch);
    }
  }

  private GitInfo updateBranch(GitInfo branch, String realRepoOrg, String realRepoName, boolean shouldBeActive) {
    return new GitInfo(branch.getId(),
        branch.getHost(),
        realRepoOrg,
        realRepoName,
        branch.getRepositoryId(),
        branch.getBranch(),
        shouldBeActive,
        branch.getCreatedTimestamp(),
        branch.getUpdatedTimestamp());
  }
}
