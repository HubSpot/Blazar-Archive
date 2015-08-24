package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.service.BranchService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/branch")
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {
  private final BranchService branchService;

  @Inject
  public BranchResource(BranchService branchService) {
    this.branchService = branchService;
  }

  @GET
  @Path("/lookup")
  public Optional<GitInfo> lookup(@QueryParam("host") String host,
                                  @QueryParam("organization") String organization,
                                  @QueryParam("repository") String repository,
                                  @QueryParam("branch") String branch) {
    return branchService.lookup(host, organization, repository, branch);
  }
}
