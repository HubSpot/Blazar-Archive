package com.hubspot.blazar.resources;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleService;

@Path("/interProject/builds")
@Produces(MediaType.APPLICATION_JSON)
public class InterProjectBuildResource {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildResource.class);
  private final DependenciesService dependenciesService;
  private InterProjectBuildService interProjectBuildService;
  private BranchService branchService;
  private final ModuleService moduleService;

  @Inject
  public InterProjectBuildResource(DependenciesService dependenciesService,
                                   InterProjectBuildService interProjectBuildService,
                                   BranchService branchService,
                                   ModuleService moduleService) {
    this.dependenciesService = dependenciesService;
    this.interProjectBuildService = interProjectBuildService;
    this.branchService = branchService;
    this.moduleService = moduleService;
  }

  @GET
  @Path("/module/{id}")
  public DependencyGraph getGraphForModule(@PathParam("id") int id) {
    return dependenciesService.buildInterProjectDependencyGraph(ImmutableSet.of(moduleService.get(id).get()));
  }

  @POST
  @Path("/start")
  public void startBuild(Set<Integer> moduleIds) {
    InterProjectBuild build = InterProjectBuild.getQueuedBuild(moduleIds, BuildTrigger.forUser("testing123"));
    interProjectBuildService.enqueue(build);
  }


  @GET
  @Path("test")
  public void runTests() {
    Set<GitInfo> allMasterBranches = branchService.getAllByBranch("master");
    Set<Module> allModsToBeProcessed = new HashSet<>();
    for (GitInfo g : allMasterBranches) {
      Set<Module> modules = moduleService.getByBranch(g.getId().get());
      for (Module m : modules) {
        if (m.getType().equals("maven") && m.getGlob().equals("**")) {
          allModsToBeProcessed.add(m);
        }
      }
    }
    LOG.info("Found {} modules to to build trees for.", allModsToBeProcessed.size());
    for (Module m : allModsToBeProcessed) {
      dependenciesService.buildInterProjectDependencyGraph(ImmutableSet.of(m));
    }
  }
}
