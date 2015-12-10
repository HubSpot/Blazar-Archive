package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/builds/history")
@Produces(MediaType.APPLICATION_JSON)
public class BuildHistoryResource {
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;

  @Inject
  public BuildHistoryResource(RepositoryBuildService repositoryBuildService, ModuleBuildService moduleBuildService) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
  }

  @GET
  @Path("/branch/{id}")
  @PropertyFiltering
  public List<RepositoryBuild> getByBranch(@PathParam("id") int branchId) {
    return repositoryBuildService.getByBranch(branchId);
  }

  @GET
  @Path("/branch/{id}/build/{number}")
  @PropertyFiltering
  public Optional<RepositoryBuild> getByBranch(@PathParam("id") int branchId, @PathParam("number") int buildNumber) {
    return repositoryBuildService.getByBranchAndNumber(branchId, buildNumber);
  }

  @GET
  @Path("/module/{id}")
  @PropertyFiltering
  public List<ModuleBuild> getByModule(@PathParam("id") int moduleId) {
    return moduleBuildService.getByModule(moduleId);
  }

  @GET
  @Path("/module/{id}/build/{number}")
  @PropertyFiltering
  public Optional<ModuleBuild> getByModule(@PathParam("id") int moduleId, @PathParam("number") int buildNumber) {
    return moduleBuildService.getByModuleAndNumber(moduleId, buildNumber);
  }
}
