package com.hubspot.blazar.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.D3GraphData;
import com.hubspot.blazar.base.D3GraphLink;
import com.hubspot.blazar.base.D3GraphNode;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleService;

@Path("/interProject/builds")
@Produces(MediaType.APPLICATION_JSON)
public class InterProjectBuildResource {
  private final DependenciesService dependenciesService;
  private InterProjectBuildService interProjectBuildService;
  private InterProjectBuildMappingService interProjectBuildMappingService;
  private BranchService branchService;
  private final ModuleService moduleService;

  @Inject
  public InterProjectBuildResource(DependenciesService dependenciesService,
                                   InterProjectBuildService interProjectBuildService,
                                   InterProjectBuildMappingService interProjectBuildMappingService,
                                   BranchService branchService,
                                   ModuleService moduleService) {
    this.dependenciesService = dependenciesService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
    this.branchService = branchService;
    this.moduleService = moduleService;
  }

  @GET
  @Path("/module/{id}")
  public DependencyGraph getGraphForModule(@PathParam("id") int id) {
    return dependenciesService.buildInterProjectDependencyGraph(ImmutableSet.of(moduleService.get(id).get()));
  }

  @POST @Path("/start") public void startBuild(Set<Integer> moduleIds) {
    InterProjectBuild build = InterProjectBuild.getQueuedBuild(moduleIds, BuildTrigger.forUser("testing123"));
    interProjectBuildService.enqueue(build);
  }

  @GET
  @Path("/{id}")
  public D3GraphData getFrontendGraph(@PathParam("id") long interProjectBuildId) {
    InterProjectBuild build = interProjectBuildService.getWithId(interProjectBuildId).get();
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    List<D3GraphLink> links = new ArrayList<>();
    List<D3GraphNode> nodes = new ArrayList<>();

    Map<Integer, GitInfo> gitInfoMap = new HashMap<>();
    Map<Integer, Module> moduleMap = new HashMap<>();

    for (InterProjectBuildMapping mapping : mappings) {
      Module module = getModuleWithCache(moduleMap, mapping.getModuleId());
      GitInfo gitInfo = getGitInfoWithCache(gitInfoMap, mapping.getRepoId());
      String source = String.format("%s-%s", gitInfo.getRepository(), module.getName());
      D3GraphNode node = new D3GraphNode(source, module.getId().get(), 100, 100, mapping.getState());
      nodes.add(node);
    }

    int pos = 0;
    for (D3GraphNode node : nodes) {
      Set<Integer> outgoingMoudles = build.getDependencyGraph().get().outgoingVertices(node.getModuleId());
      for (int module : outgoingMoudles) {
        links.add(new D3GraphLink(getPos(nodes, module), pos));
      }
      pos++;
    }
    return new D3GraphData(links, nodes);
  }

  private int getPos(List<D3GraphNode> nodes, int moduleId) {
    int pos = 0;
    for (D3GraphNode node : nodes) {
      if (node.getModuleId() == moduleId) {
        return pos;
      }
      pos++;
    }
    throw new IllegalStateException(String.format("No node with moduleId %d", moduleId));
  }

  private Module getModuleWithCache (Map<Integer, Module> moduleMap, int id) {
    Module module;
    if (moduleMap.containsKey(id)) {
      module = moduleMap.get(id);
    } else {
      module = moduleService.get(id).get();
      moduleMap.put(id, module);
    }
    return module;
  }

  private GitInfo getGitInfoWithCache(Map<Integer, GitInfo> gitInfoMap, int id) {
    GitInfo gitInfo;
    if (gitInfoMap.containsKey(id)) {
      gitInfo = gitInfoMap.get(id);
    } else {
      gitInfo = branchService.get(id).get();
      gitInfoMap.put(id, gitInfo);
    }
    return gitInfo;
  }
}
