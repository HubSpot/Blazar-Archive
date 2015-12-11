package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/branches")
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {

  @Inject
  public BranchResource(BranchService branchService, ModuleService moduleService) {

  }

  @GET
  @PropertyFiltering
  public Set<RepositoryState> getAll() {
    // TODO
    return null;
  }

  @GET
  @Path("/{id}")
  @PropertyFiltering
  public Optional<RepositoryState> get(@PathParam("id") int branchId) {
    // TODO
    return null;
  }

  @GET
  @Path("/{id}/modules")
  @PropertyFiltering
  public Set<ModuleState> getModules(@PathParam("id") int branchId) {
    // TODO
    return null;
  }
}
