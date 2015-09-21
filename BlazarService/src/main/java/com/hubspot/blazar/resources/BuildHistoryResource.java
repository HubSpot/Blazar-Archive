package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
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
    BuildDefinition definition = buildDefinitionService.getByModule(moduleId).get();

    List<ModuleBuild> builds = new ArrayList<>();
    for (Build build : buildService.getAllByModule(definition.getModule())) {
      builds.add(new ModuleBuild(definition, build));
    }

    return builds;
  }

  @GET
  @Path("/module/{id}/build/{buildNumber}")
  @PropertyFiltering
  public Optional<ModuleBuild> getByModule(@PathParam("id") int moduleId, @PathParam("buildNumber") int buildNumber) {
    BuildDefinition definition = buildDefinitionService.getByModule(moduleId).get();
    Optional<Build> build = buildService.getByModuleAndNumber(definition.getModule(), buildNumber);

    if (build.isPresent()) {
      return Optional.of(new ModuleBuild(definition, build.get()));
    } else {
      return Optional.absent();
    }
  }
}
