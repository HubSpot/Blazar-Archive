package com.hubspot.blazar.resources;

import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.ModuleService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/module")
@Produces(MediaType.APPLICATION_JSON)
public class ModuleResource {
  private final ModuleService moduleService;

  @Inject
  public ModuleResource(ModuleService moduleService) {
    this.moduleService = moduleService;
  }

  @GET
  @Path("/branch/{id}")
  public Set<Module> getByBranch(@PathParam("id") int branchId) {
    return moduleService.getByBranch(branchId);
  }
}
