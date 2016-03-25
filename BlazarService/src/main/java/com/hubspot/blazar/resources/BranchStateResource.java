package com.hubspot.blazar.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.cctray.CCTrayProject;
import com.hubspot.blazar.cctray.CCTrayProjectFactory;
import com.hubspot.blazar.cctray.CCTrayWrapper;
import com.hubspot.blazar.data.service.StateService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

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
  @Path("/{id}/shield")
  @Produces("image/png")
  public StreamingOutput getShield(@PathParam("id") int branchId) {
    Optional<RepositoryState> state = stateService.getRepositoryState(branchId);
    if (!state.isPresent()) {
      throw new IllegalArgumentException(String.format("No state for id  %d", branchId));
    }

    final String path;
    if (state.get().getLastBuild().isPresent()) {
      path = pickImage(state.get().getLastBuild().get().getState());
    } else {
      path = "shields/build-???-lightgrey.png";
    }

    return new StreamingOutput() {
      @Override
      public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        ByteStreams.copy(Resources.getResource(path).openStream(), outputStream);
      }
    };
  }

  @GET
  @Path("/{id}/modules")
  @PropertyFiltering
  public Set<ModuleState> getModules(@PathParam("id") int branchId) {
    return stateService.getModuleStatesByBranch(branchId);
  }

  private static String pickImage(RepositoryBuild.State state) {
    switch (state) {
      case FAILED:
        return "shields/build-failing-red.png";
      case SUCCEEDED:
        return "shields/build-passing-green.png";
      case CANCELLED:
        return "shields/build-cancelled-yellow.png";
      case UNSTABLE:
        return "shields/build-unstable-red.png";
      default:
        return "shields/build-???-lightgrey.png";
    }
  }
}
