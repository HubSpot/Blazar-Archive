package com.hubspot.blazar.util;

import javax.ws.rs.core.UriBuilder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.UiConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

@Singleton
public class BlazarUrlHelper {

  private final BranchService branchService;
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleService moduleService;
  private final UiConfiguration uiConfiguration;

  @Inject
  public BlazarUrlHelper(BranchService branchService,
                         RepositoryBuildService repositoryBuildService,
                         ModuleService moduleService,
                         BlazarConfiguration configuration) {
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.moduleService = moduleService;

    this.uiConfiguration = configuration.getUiConfiguration();
  }

  public String getBlazarUiLink(RepositoryBuild build) {
    GitInfo gitInfo = branchService.get(build.getBranchId()).get();
    return UriBuilder.fromUri(uiConfiguration.getBaseUrl())
        .segment("builds")
        .segment(gitInfo.getHost())
        .segment(gitInfo.getOrganization())
        .segment(gitInfo.getRepository())
        .segment(gitInfo.getBranch())
        .segment(String.valueOf(build.getBuildNumber()))
        .build()
        .toString();
  }

  public String getBlazarUiLink(ModuleBuild build) {
    RepositoryBuild repoBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    GitInfo gitInfo = branchService.get(repoBuild.getBranchId()).get();
    Module module = moduleService.get(build.getModuleId()).get();
    return UriBuilder.fromUri(uiConfiguration.getBaseUrl())
        .segment("builds")
        .segment(gitInfo.getHost())
        .segment(gitInfo.getOrganization())
        .segment(gitInfo.getRepository())
        .segment(gitInfo.getBranch())
        .segment(String.valueOf(repoBuild.getBuildNumber()))
        .segment(module.getName())
        .build()
        .toString();
  }
}
