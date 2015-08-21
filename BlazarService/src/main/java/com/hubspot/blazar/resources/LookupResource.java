package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/lookup")
@Produces(MediaType.APPLICATION_JSON)
public class LookupResource {
  private final BuildDefinitionService buildDefinitionService;
  private final ModuleService moduleService;

  @Inject
  public LookupResource(BuildDefinitionService buildDefinitionService,
                        ModuleService moduleService) {
    this.buildDefinitionService = buildDefinitionService;
    this.moduleService = moduleService;
  }

  @GET
  @Path("/{host}/{organization}/{repository}/{branch}/{module}")
  @PropertyFiltering
  public Module getAllBuildDefinitions(@PathParam("host") String host,
                                                     @PathParam("organization") String organization,
                                                     @PathParam("repository") String repository,
                                                     @PathParam("branch") String branch,
                                                     @PathParam("module") String module) {
    GitInfo gitInfo = new GitInfo(Optional.<Integer>absent(), host, organization, repository, branch);
    return moduleService.getModule(gitInfo, module);
  }

}
