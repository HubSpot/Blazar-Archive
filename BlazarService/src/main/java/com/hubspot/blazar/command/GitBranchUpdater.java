package com.hubspot.blazar.command;

import java.io.IOException;

import net.sourceforge.argparse4j.inf.Namespace;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.util.GitHubHelper;

import io.dropwizard.setup.Bootstrap;

/**
 *
 * GitHub repositories can change owners and names, and be deleted without Blazar
 * getting an event to update its metadata. Because we don't receive events this
 * cleaner is run against all repos periodically to ensure that we delete anything
 * that has been deleted or moved to a non-managed organization.
 *
 * @see CleanRepoMetadataCommand#run(Bootstrap, Namespace, BlazarConfiguration)
 * for the DropWizard command
 *
 * This runnable checks GitHub for the current metadata about a single repository
 * If the information is different from what Blazar knows we update that
 * information accordingly:
 *
 * - If the repository does not exist, we delete it.
 * - If the repository name has changed, we update it.
 * - If the organization name has changed, we update it.
 * - If the organization name is not a managed organization, we delete it.
 *
 */
public class GitBranchUpdater implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(GitBranchUpdater.class);

  private final GitHubHelper gitHubHelper;
  private final BlazarConfiguration configuration;
  private final BranchService branchService;
  private final GitInfo oldBranch;

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
      LOG.warn("No git host configured for {}/{}#{} marking as inactive", oldBranch.getHost(), oldBranch.getFullRepositoryName(), oldBranch.getBranch());
      branchService.delete(oldBranch);
      return;
    }

    GitHubConfiguration githubConfiguration = configuration.getGitHubConfiguration().get(oldBranch.getHost());
    GHRepository ghRepository;
    try {
      ghRepository = gitHubHelper.repositoryFor(oldBranch);
    } catch (IOException e) {
      LOG.error("Caught exception while trying to find {} in github", oldBranch, e);
      throw new RuntimeException(e);
    }

    if (ghRepository.getId() != oldBranch.getRepositoryId()) {
      LOG.warn("A repository with a different github id {} has replaced {}. Considering {}#{} as deleted ", ghRepository.getId(), oldBranch, oldBranch.getFullRepositoryName(), oldBranch.getBranch());
      branchService.delete(oldBranch);
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
    if (orgNameChanged || repoNameChanged || !orgIsConfigured) {
      boolean active = orgIsConfigured;
      GitInfo updatedBranch = updateBranch(oldBranch, newRepoOrg, newRepoName, active);
      LOG.info("Branch {} has changed updating to {}", oldBranch, updatedBranch);
      branchService.upsert(updatedBranch);
    }
  }

  private GitInfo updateBranch(GitInfo branch, String realRepoOrg, String realRepoName, boolean active) {
    return new GitInfo(branch.getId(),
        branch.getHost(),
        realRepoOrg,
        realRepoName,
        branch.getRepositoryId(),
        branch.getBranch(),
        active,
        branch.getCreatedTimestamp(),
        System.currentTimeMillis());
  }
}
