package com.hubspot.blazar.data.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDiscoveryResult;

@Singleton
public class ModuleDiscoveryService {
  private final ModuleService moduleService;
  private final MalformedFileService malformedFileService;

  @Inject
  public ModuleDiscoveryService(ModuleService moduleService, MalformedFileService malformedFileService) {
    this.moduleService = moduleService;
    this.malformedFileService = malformedFileService;
  }

  /**
   * It return
   * @param branch
   * @param moduleDiscoveryResult
   * @return
   */
  @Transactional
  public void persistDiscoveryResult(GitInfo branch, ModuleDiscoveryResult moduleDiscoveryResult) {
    moduleService.persistModulesAndDependencies(branch, moduleDiscoveryResult.getModules());
    malformedFileService.setMalformedFiles(branch, moduleDiscoveryResult.getMalformedFiles());
  }
}
