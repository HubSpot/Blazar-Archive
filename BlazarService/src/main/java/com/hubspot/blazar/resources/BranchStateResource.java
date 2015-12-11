package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.service.StateService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/branches/state")
@Produces(MediaType.APPLICATION_JSON)
public class BranchStateResource {
  private final StateService stateService;

  @Inject
  public BranchStateResource(StateService stateService) {
    this.stateService = stateService;
  }

  @GET
  @PropertyFiltering
  public Set<RepositoryState> getAll() {
    return stateService.getAllRepositoryStates();
  }

  @GET
  @Path("/{id}")
  @PropertyFiltering
  public Optional<RepositoryState> get(@PathParam("id") int branchId) {
    return stateService.getRepositoryState(branchId);
  }

  @GET
  @Path("/{id}/modules")
  @PropertyFiltering
  public Set<ModuleState> getModules(@PathParam("id") int branchId) {
    return stateService.getModuleStatesByBranch(branchId);
  }
}
