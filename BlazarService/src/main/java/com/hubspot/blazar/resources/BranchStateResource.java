package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.cctray.CCTrayProjectFactory;
import com.hubspot.blazar.cctray.CCTrayWrapper;
import com.hubspot.blazar.data.service.StateService;
import com.hubspot.blazar.cctray.CCTrayProject;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

@Path("/branches/state")
@Produces(MediaType.APPLICATION_JSON)
public class BranchStateResource {
  private final StateService stateService;
  private final CCTrayProjectFactory ccTrayProjectFactory;

  @Inject
  public BranchStateResource(StateService stateService, CCTrayProjectFactory ccTrayProjectFactory) {
    this.stateService = stateService;
    this.ccTrayProjectFactory = ccTrayProjectFactory;
  }

  @GET
  @PropertyFiltering
  public Set<RepositoryState> getAll() {
    return stateService.getAllRepositoryStates();
  }

  @GET
  @Path("/cc.xml")
  @Produces(MediaType.APPLICATION_XML)
  public CCTrayWrapper getCCTrayXml() {
    Set<CCTrayProject> projects = new HashSet<>();
    for (RepositoryState repositoryState : getAll()) {
      Optional<CCTrayProject> maybeProject = ccTrayProjectFactory.apply(repositoryState);
      if (maybeProject != null && maybeProject.isPresent()) {
        projects.add(maybeProject.get());
      }
    }

    return new CCTrayWrapper(projects);
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
