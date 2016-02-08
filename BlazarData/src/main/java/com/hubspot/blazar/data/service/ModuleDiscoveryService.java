package com.hubspot.blazar.data.service;

import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
public class ModuleDiscoveryService {
  private final ModuleService moduleService;
  private final MalformedFileService malformedFileService;

  @Inject
  public ModuleDiscoveryService(ModuleService moduleService, MalformedFileService malformedFileService) {
    this.moduleService = moduleService;
    this.malformedFileService = malformedFileService;
  }

  @Transactional
  public void handleDiscoveryResult(GitInfo gitInfo, DiscoveryResult result) {
    moduleService.setModules(gitInfo, result.getModules());
    malformedFileService.setMalformedFiles(gitInfo, result.getMalformedFiles());
  }
}
