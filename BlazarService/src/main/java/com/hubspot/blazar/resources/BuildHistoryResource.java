package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleActivityPage;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/builds/history")
@Produces(MediaType.APPLICATION_JSON)
public class BuildHistoryResource {
  private final RepositoryBuildService repositoryBuildService;
  private BranchService branchService;
  private ModuleService moduleService;
  private final ModuleBuildService moduleBuildService;
  private static final Logger LOG = LoggerFactory.getLogger(BuildHistoryResource.class);

  @Inject
  public BuildHistoryResource(RepositoryBuildService repositoryBuildService,
                              BranchService branchService,
                              ModuleService moduleService,
                              ModuleBuildService moduleBuildService) {
    this.repositoryBuildService = repositoryBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
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
    branchService.checkBranchExists(branchId);
    return repositoryBuildService.getByBranchAndNumber(branchId, buildNumber);
  }

  @GET
  @Path("/module/{moduleId}")
  @PropertyFiltering
  public ModuleActivityPage getByModule(@PathParam("moduleId") int moduleId,
                                        @QueryParam("fromBuildNumber") Optional<Integer> maybeFromBuildNumber,
                                        @QueryParam("pageSize") Optional<Integer> maybePageSize) {
    moduleService.checkModuleExists(moduleId);
    return moduleBuildService.getModuleBuildHistoryPage(moduleId, maybeFromBuildNumber, maybePageSize);
  }

  @GET
  @Path("/module/{id}/build/{number}")
  @PropertyFiltering
  public Optional<ModuleBuild> getByModule(@PathParam("id") int moduleId, @PathParam("number") int buildNumber) {
    moduleService.checkModuleExists(moduleId);
    return moduleBuildService.getByModuleAndNumber(moduleId, buildNumber);
  }
}
