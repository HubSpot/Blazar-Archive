package com.hubspot.blazar.guice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sourceforge.argparse4j.inf.Namespace;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.util.GitHubHelper;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

public class CleanMissingReposCommand extends ConfiguredCommand<BlazarConfiguration> {
  private static String COMMAND_NAME = "clean_missing_repos";
  private static String COMMAND_DESC = "Finds repos no longer in the managed organizations and marks all branches as inactive";
  private static final Logger LOG = LoggerFactory.getLogger(CleanMissingReposCommand.class);
  private final ExecutorService executorService;


  public CleanMissingReposCommand() {
    super(COMMAND_NAME, COMMAND_DESC);
    this.executorService = Executors.newFixedThreadPool(1);
  }

  @Override
  protected void run(Bootstrap<BlazarConfiguration> bootstrap,
                     Namespace namespace,
                     BlazarConfiguration configuration) throws Exception {
    Injector i = Guice.createInjector(new CommandModule(bootstrap, configuration));
    try {
      GitHubHelper gitHubHelper = i.getInstance(GitHubHelper.class);
      BranchService branchService = i.getInstance(BranchService.class);
      Set<GitInfo> branches = branchService.getAllActive();
      Map<GitInfo, Future<GitInfo>> futureMap = new HashMap<>();

      for (GitInfo branch : branches) {
        futureMap.put(branch, executorService.submit(new Task(branch, configuration, branchService, gitHubHelper)));
      }

      for (Map.Entry<GitInfo, Future<GitInfo>> entry : futureMap.entrySet()) {
        GitInfo oldBranch = entry.getKey();
        GitInfo newBranch = entry.getValue().get();  // todo catch exceptions
        if (!oldBranch.equals(newBranch)) {
          LOG.info("Moved {} to {}", oldBranch, newBranch);
        }
      }
    } finally {
      // todo shutdown things
      executorService.shutdownNow();
    }
  }


  private class Task implements Callable<GitInfo> {

    private final GitInfo branch;
    private BlazarConfiguration configuration;
    private final BranchService branchService;
    private final GitHubHelper gitHubHelper;

    public Task(GitInfo branch,
                BlazarConfiguration configuration,
                BranchService branchService,
                GitHubHelper gitHubHelper) {
      this.branch = branch;
      this.configuration = configuration;
      this.branchService = branchService;
      this.gitHubHelper = gitHubHelper;
    }

    // returns true if we archived the repo
    @Override
    public GitInfo call() throws Exception {

      // If no such github is configured, remove the branch
      if (!configuration.getGitHubConfiguration().containsKey(branch.getHost())) {
        LOG.warn("No host configured for branch {} marking as inactive", branch);
        branchService.delete(branch);
        return branchService.get(branch.getId().get()).get();
      }

      GHRepository repo = gitHubHelper.repositoryFor(branch);
      String realRepoName = repo.getName();
      String realRepoOrg = repo.getOwnerName();
      LOG.debug("Searching for {} in github found {}", branch, repo.getFullName());
      if (!branch.getRepository().equals(realRepoName) || !branch.getOrganization().equals(realRepoOrg)) {
        LOG.info("{} has been renamed or moved to {} updating blazar's records", branch, repo.getFullName());
        GitInfo newBranch = new GitInfo(branch.getId(), branch.getHost(), realRepoOrg, realRepoName, branch.getRepositoryId(), branch.getBranch(), branch.isActive(), branch.getCreatedTimestamp(), branch.getUpdatedTimestamp());
        List<String> orgs = configuration.getGitHubConfiguration().get(newBranch.getHost()).getOrganizations();
        if(!orgs.contains(branch.getOrganization())) {
          // not in maintained org mark as inactive
          LOG.info("{} is not in a maintained org {}, marking as inactive", newBranch, orgs);
          branchService.delete(newBranch);
        }
        return branchService.get(newBranch.getId().get()).get();
      }
      return branch;
    }
  }
}
