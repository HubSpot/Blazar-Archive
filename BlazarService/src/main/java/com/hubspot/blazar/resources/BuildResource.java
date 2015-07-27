package com.hubspot.blazar.resources;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/build")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {
  private final BuildDefinitionService buildDefinitionService;
  private final BuildStateService buildStateService;

  @Inject
  public BuildResource(BuildDefinitionService buildDefinitionService, BuildStateService buildStateService) {
    this.buildDefinitionService = buildDefinitionService;
    this.buildStateService = buildStateService;
  }

  @GET
  @Path("/definitions")
  @PropertyFiltering
  public Set<BuildDefinition> getAllBuildDefinitions() {
    return buildDefinitionService.getAllBuildDefinitions();
  }

  @GET
  @Path("/states")
  public Set<BuildState> getAllBuildStates() {
    return buildStateService.getAllBuildStates();
  }
}
