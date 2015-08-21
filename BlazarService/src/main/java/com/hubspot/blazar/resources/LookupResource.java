package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/lookup")
@Produces(MediaType.APPLICATION_JSON)
public class LookupResource {
  private final BuildService buildService;
  private final ModuleService moduleService;

  @Inject
  public LookupResource(BuildService buildService,
                        ModuleService moduleService) {
    this.buildService = buildService;
    this.moduleService = moduleService;
  }

  @GET
  @Path("/{host}/{organization}/{repository}/{branch}/{module}")
  @PropertyFiltering
  public Optional<Module> lookupModule(@PathParam("host") String host,
                                                     @PathParam("organization") String organization,
                                                     @PathParam("repository") String repository,
                                                     @PathParam("branch") String branch,
                                                     @PathParam("module") String module) {
    GitInfo gitInfo = new GitInfo(Optional.<Integer>absent(), host, organization, repository, branch);
    return moduleService.getModule(gitInfo, module);
  }

  @GET
  @Path("/{host}/{organization}/{repository}/{branch}/{module}/{buildNumber}")
  @PropertyFiltering
  public Optional<ModuleBuild> lookupBuild(@PathParam("host") String host,
                                       @PathParam("organization") String organization,
                                       @PathParam("repository") String repository,
                                       @PathParam("branch") String branch,
                                       @PathParam("module") String module,
                                       @PathParam("buildNumber") Integer buildNumber) {
    GitInfo gitInfo = new GitInfo(Optional.<Integer>absent(), host, organization, repository, branch);
    return buildService.get(gitInfo, module, buildNumber);
  }

}
