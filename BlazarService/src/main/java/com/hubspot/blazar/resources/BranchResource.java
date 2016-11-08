package com.hubspot.blazar.resources;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BranchSetting;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.branch.BranchStatus;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BranchSettingsService;
import com.hubspot.blazar.data.service.BranchStatusService;
import com.hubspot.blazar.data.service.MalformedFileService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

@Path("/branches")
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {
  private final BranchService branchService;
  private final BranchStatusService branchStatusService;
  private final BranchSettingsService branchSettingsService;
  private final ModuleService moduleService;
  private final MalformedFileService malformedFileService;
  private final ModuleDiscoveryService moduleDiscoveryService;
  private final ModuleDiscovery moduleDiscovery;

  @Inject
  public BranchResource(BranchService branchService,
                        BranchStatusService branchStatusService,
                        BranchSettingsService branchSettingsService,
                        ModuleService moduleService,
                        MalformedFileService malformedFileService,
                        ModuleDiscoveryService moduleDiscoveryService,
                        ModuleDiscovery moduleDiscovery) {
    this.branchService = branchService;
    this.branchStatusService = branchStatusService;
    this.branchSettingsService = branchSettingsService;
    this.moduleService = moduleService;
    this.malformedFileService = malformedFileService;
    this.moduleDiscoveryService = moduleDiscoveryService;
    this.moduleDiscovery = moduleDiscovery;
  }

  @GET
  @PropertyFiltering
  public Set<GitInfo> getAll() {
    return branchService.getAll();
  }

  @GET
  @Path("/repo/{repositoryId}")
  @PropertyFiltering
  public Set<GitInfo> getByRepositoryId(@PathParam("repositoryId") int repositoryId) {
    return branchService.getByRepository(repositoryId);
  }

  @GET
  @Path("/{id}")
  @PropertyFiltering
  public Optional<GitInfo> get(@PathParam("id") int branchId) {
    return branchService.get(branchId);
  }

  @GET
  @Path("/{id}/modules")
  @PropertyFiltering
  public Set<Module> getModules(@PathParam("id") int branchId) {
    return moduleService.getByBranch(branchId);
  }

  @GET
  @Path("/{branchId}/status")
  @PropertyFiltering
  public Optional<BranchStatus> getBranchStatusById(@PathParam("branchId") int branchId) {
    return branchStatusService.getBranchStatusById(branchId);
  }

  @GET
  @Path("/{id}/malformedFiles")
  @PropertyFiltering
  public Set<MalformedFile> getMalformedFiles(@PathParam("id") int branchId) {
    return malformedFileService.getMalformedFiles(branchId);
  }

  @POST
  @Path("/{id}/discover")
  @PropertyFiltering
  public DiscoveryResult discoverModules(@PathParam("id") int branchId) throws IOException {
    GitInfo gitInfo = get(branchId).get();
    DiscoveryResult result = moduleDiscovery.discover(gitInfo);
    moduleDiscoveryService.handleDiscoveryResult(gitInfo, result);
    return result;
  }

  @GET
  @Path("{id}/discover")
  @PropertyFiltering
  public DiscoveryResult sideEffectFreeModuleDiscovery(@PathParam("id") int branchId) throws IOException {
    GitInfo gitInfo = get(branchId).get();
    return moduleDiscovery.discover(gitInfo);
 }

  @GET
  @Path("{id}/settings")
  @PropertyFiltering
  public BranchSetting getBranchSettings(@PathParam("id") int branchId) {
    Optional<BranchSetting> maybeBranchSetting = branchSettingsService.getByBranchId(branchId);
    Optional<GitInfo> gitInfo = branchService.get(branchId);
    if (maybeBranchSetting.isPresent()) {
      return maybeBranchSetting.get();
    }
    if (gitInfo.isPresent() && gitInfo.get().getBranch().equals("master")) {
      return BranchSetting.getWithDefaultSettingsForMaster(branchId);
    }
    return BranchSetting.getWithDefaultSettings(branchId);
  }

  @PUT
  @Path("{id}/settings")
  public void updateSetting(@PathParam("id") int branchId, BranchSetting branchSetting) {
    Optional<BranchSetting> maybeBranchSetting = branchSettingsService.getByBranchId(branchId);
    if (maybeBranchSetting.isPresent()) {
      branchSettingsService.update(new BranchSetting(branchId, branchSetting.isTriggerInterProjectBuilds(), branchSetting.isInterProjectBuildOptIn()));
    } else {
      branchSettingsService.insert(new BranchSetting(branchId, branchSetting.isTriggerInterProjectBuilds(), branchSetting.isInterProjectBuildOptIn()));
    }
  }
}
