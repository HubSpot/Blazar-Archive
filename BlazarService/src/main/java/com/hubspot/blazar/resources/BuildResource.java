package com.hubspot.blazar.resources;

import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

@Path("/build")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {
  private final BuildDefinitionService buildDefinitionService;
  private final BuildStateService buildStateService;
  private final BuildService buildService;

  @Inject
  public BuildResource(BuildDefinitionService buildDefinitionService,
                       BuildStateService buildStateService,
                       BuildService buildService) {
    this.buildDefinitionService = buildDefinitionService;
    this.buildStateService = buildStateService;
    this.buildService = buildService;
  }

  @GET
  @Path("/definitions")
  @PropertyFiltering
  public Set<BuildDefinition> getAllBuildDefinitions(@QueryParam("since") @DefaultValue("0") long since) {
    return buildDefinitionService.getAllBuildDefinitions(since);
  }

  @GET
  @Path("/states")
  @PropertyFiltering
  public Set<BuildState> getAllBuildStates(@QueryParam("since") @DefaultValue("0") long since) {
    return buildStateService.getAllBuildStates(since);
  }

  @GET
  @Path("/{id}")
  public Optional<ModuleBuild> get(@PathParam("id") long id) {
    return buildService.get(id);
  }

  @POST
  public ModuleBuild trigger(BuildDefinition buildDefinition) {
    BuildState buildState = buildService.enqueue(buildDefinition);

    return new ModuleBuild(buildState.getGitInfo(), buildState.getModule(), buildState.getPendingBuild().get());
  }

  @PUT
  public ModuleBuild update(ModuleBuild moduleBuild) {
    buildService.update(moduleBuild.getBuild());
    return moduleBuild;
  }

}
