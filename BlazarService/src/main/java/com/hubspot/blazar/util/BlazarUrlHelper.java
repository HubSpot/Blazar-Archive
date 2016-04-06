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
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

import java.net.URI;

@Singleton
public class BlazarUrlHelper {
  private final RepositoryBuildService repositoryBuildService;
  private final ModuleService moduleService;
  private final UiConfiguration uiConfiguration;

  @Inject
  public BlazarUrlHelper(RepositoryBuildService repositoryBuildService,
                         ModuleService moduleService,
                         BlazarConfiguration configuration) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleService = moduleService;

    this.uiConfiguration = configuration.getUiConfiguration();
  }

  public URI getBlazarUiLink(GitInfo gitInfo) {
    return getBlazarUiLink(gitInfo.getId().get());
  }

  public String getBlazarUiLink(RepositoryBuild build) {
    return UriBuilder.fromUri(getBlazarUiLink(build.getBranchId()))
        .segment("build")
        .segment(Integer.toString(build.getBuildNumber()))
        .build()
        .toString();
  }

  public String getBlazarUiLink(ModuleBuild build) {
    RepositoryBuild repoBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    Module module = moduleService.get(build.getModuleId()).get();
    return UriBuilder.fromUri(getBlazarUiLink(repoBuild))
        .segment("module")
        .segment(module.getName())
        .build()
        .toString();
  }

  private URI getBlazarUiLink(int branchId) {
    return UriBuilder.fromUri(uiConfiguration.getBaseUrl())
        .segment("builds")
        .segment("branch")
        .segment(Integer.toString(branchId))
        .build();
  }
}
