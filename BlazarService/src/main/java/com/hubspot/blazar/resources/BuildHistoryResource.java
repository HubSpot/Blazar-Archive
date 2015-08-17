package com.hubspot.blazar.resources;

import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/build/history")
@Produces(MediaType.APPLICATION_JSON)
public class BuildHistoryResource {
  private final BuildDefinitionService buildDefinitionService;
  private final BuildService buildService;

  @Inject
  public BuildHistoryResource(BuildDefinitionService buildDefinitionService, BuildService buildService) {
    this.buildDefinitionService = buildDefinitionService;
    this.buildService = buildService;
  }

  @GET
  @Path("/module/{id}")
  @PropertyFiltering
  public List<ModuleBuild> getByModule(@PathParam("id") int moduleId) {
    BuildDefinition definition = buildDefinitionService.getByModuleId(moduleId).get();

    List<ModuleBuild> builds = new ArrayList<>();
    for (Build build : buildService.getByModule(definition.getModule())) {
      builds.add(new ModuleBuild(definition, build));
    }

    return builds;
  }
}
