package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.blazar.data.service.ModuleService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

@Path("/branch")
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {
  private final BuildStateService buildStateService;
  private final BranchService branchService;
  private final ModuleService moduleService;

  @Inject
  public BranchResource(BuildStateService buildStateService, BranchService branchService, ModuleService moduleService) {
    this.buildStateService = buildStateService;
    this.branchService = branchService;
    this.moduleService = moduleService;
  }

  @GET
  @Path("/lookup")
  public Optional<GitInfo> lookup(@QueryParam("host") String host,
                                  @QueryParam("organization") String organization,
                                  @QueryParam("repository") String repository,
                                  @QueryParam("branch") String branch) {
    return branchService.lookup(host, organization, repository, branch);
  }

  @GET
  @Path("/search")
  public Set<GitInfo> search(@QueryParam("host") Optional<String> host,
                             @QueryParam("organization") Optional<String> organization,
                             @QueryParam("repository") Optional<String> repository,
                             @QueryParam("branch") Optional<String> branch) {
    Predicate<GitInfo> query = new BranchQuery(host, organization, repository, branch);
    Set<GitInfo> matches = new HashSet<>();
    for (BuildState buildState : buildStateService.getAllActive()) {
      if (query.apply(buildState.getGitInfo())) {
        matches.add(buildState.getGitInfo());
      }
    }

    return matches;
  }

  @GET
  @Deprecated
  @Path("/{id}/modules")
  public Set<Module> getModules(@PathParam("id") int branchId) {
    return moduleService.getByBranch(branchId);
  }

  private static class BranchQuery implements Predicate<GitInfo> {
    private final Optional<String> host;
    private final Optional<String> organization;
    private final Optional<String> repository;
    private final Optional<String> branch;


    public BranchQuery(Optional<String> host,
                       Optional<String> organization,
                       Optional<String> repository,
                       Optional<String> branch) {
      this.host = host;
      this.organization = organization;
      this.repository = repository;
      this.branch = branch;
    }

    @Override
    public boolean apply(GitInfo gitInfo) {
      if (host.isPresent() && !host.get().equals(gitInfo.getHost())) {
        return false;
      } else if (organization.isPresent() && !organization.get().equals(gitInfo.getOrganization())) {
        return false;
      } else if (repository.isPresent() && !repository.get().equals(gitInfo.getRepository())) {
        return false;
      } else if (branch.isPresent() && !branch.get().equals(gitInfo.getBranch())) {
        return false;
      } else {
        return true;
      }
    }
  }
}
