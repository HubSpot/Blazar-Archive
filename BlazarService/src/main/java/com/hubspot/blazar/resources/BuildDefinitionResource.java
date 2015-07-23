package com.hubspot.blazar.resources;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/build/definitions")
@Produces(MediaType.APPLICATION_JSON)
public class BuildDefinitionResource {
  private BuildDefinitionService buildDefinitionService;

  @Inject
  public BuildDefinitionResource(BuildDefinitionService buildDefinitionService) {
    this.buildDefinitionService = buildDefinitionService;
  }

  @GET
  @PropertyFiltering
  public Set<BuildDefinition> getAllBuildDefinitions() {
    return buildDefinitionService.getAllBuildDefinitions();
  }
}
