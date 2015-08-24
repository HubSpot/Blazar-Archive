package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/branch")
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {
  private final BranchService branchService;
  private final ModuleService moduleService;

  @Inject
  public BranchResource(BranchService branchService, ModuleService moduleService) {
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
  @Path("/{id}/modules")
  public Set<Module> getModules(@PathParam("id") int branchId) {
    return moduleService.getByBranch(branchId);
  }
}
