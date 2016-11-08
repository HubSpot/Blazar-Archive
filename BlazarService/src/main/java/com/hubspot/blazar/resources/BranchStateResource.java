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
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.base.branch.BranchStatus;
import com.hubspot.blazar.cctray.CCTrayProject;
import com.hubspot.blazar.cctray.CCTrayProjectFactory;
import com.hubspot.blazar.cctray.CCTrayWrapper;
import com.hubspot.blazar.data.cache.StateCache;
import com.hubspot.blazar.data.service.BranchStatusService;
import com.hubspot.blazar.data.service.StateService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

@Path("/branches/state")
@Produces(MediaType.APPLICATION_JSON)
public class BranchStateResource {
  private final StateService stateService;
  private BranchStatusService branchStatusService;
  private final StateCache stateCache;
  private final CCTrayProjectFactory ccTrayProjectFactory;

  @Inject
  public BranchStateResource(StateService stateService,
                             BranchStatusService branchStatusService,
                             StateCache stateCache,
                             CCTrayProjectFactory ccTrayProjectFactory) {
    this.stateService = stateService;
    this.branchStatusService = branchStatusService;
    this.stateCache = stateCache;
    this.ccTrayProjectFactory = ccTrayProjectFactory;
  }

  @GET
  @PropertyFiltering
  public Set<RepositoryState> getAll() {
    return stateCache.getAllRepositoryStates();
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
  @Path("/{branchId}")
  @PropertyFiltering
  public Optional<RepositoryState> get(@PathParam("branchId") int branchId) {
    return stateService.getRepositoryState(branchId);
  }

  @GET
  @Path("/{branchId}/modules")
  @PropertyFiltering
  public Set<ModuleState> getAllModuleStatesForBranch(@PathParam("branchId") int branchId) {
    Optional<BranchStatus> status  = branchStatusService.getBranchStatusById(branchId);
    if (status.isPresent()) {
      return status.get().getModuleStates();
    } else {
      return ImmutableSet.of();
    }
  }

  @GET
  @Path("/{branchId}/shield")
  @Produces("image/svg+xml")
  public StreamingOutput getShield(@PathParam("branchId") int branchId) {
    Optional<RepositoryState> state = stateService.getRepositoryState(branchId);
    if (!state.isPresent()) {
      throw new IllegalArgumentException(String.format("No state for id  %d", branchId));
    }

    final String path;
    if (state.get().getLastBuild().isPresent()) {
      path = pickImage(state.get().getLastBuild().get().getState());
    } else {
      path = "shields/build-???-lightgrey.svg";
    }

    return new StreamingOutput() {
      @Override
      public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        ByteStreams.copy(Resources.getResource(path).openStream(), outputStream);
      }
    };
  }

  private static String pickImage(RepositoryBuild.State state) {
    switch (state) {
      case FAILED:
        return "shields/build-failing-red.svg";
      case SUCCEEDED:
        return "shields/build-passing-green.svg";
      case CANCELLED:
        return "shields/build-cancelled-yellow.svg";
      case UNSTABLE:
        return "shields/build-unstable-red.svg";
      default:
        return "shields/build-???-lightgrey.svg";
    }
  }
}
