package com.hubspot.blazar.data.service;

import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Set;

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
  public Set<Module> handleDiscoveryResult(GitInfo gitInfo, DiscoveryResult result) {
    Set<Module> modules = moduleService.setModules(gitInfo, result.getModules());
    malformedFileService.setMalformedFiles(gitInfo, result.getMalformedFiles());
    return modules;
  }
}
