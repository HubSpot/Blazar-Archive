package com.hubspot.blazar.data.service;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.dao.BuildDefinitionDao;
import com.hubspot.guice.transactional.Transactional;

import java.util.Set;

public class BuildDefinitionService {
  private final BuildDefinitionDao buildDefinitionDao;

  @Inject
  public BuildDefinitionService(BuildDefinitionDao buildDefinitionDao) {
    this.buildDefinitionDao = buildDefinitionDao;
  }

  public Set<Module> getModules(GitInfo gitInfo) {
    return buildDefinitionDao.getModules(gitInfo);
  }

  @Transactional
  public void setModules(GitInfo gitInfo, Set<Module> modules) {
    Set<Module> existing = getModules(gitInfo);

    for (Module module : Sets.difference(existing, modules)) {
      buildDefinitionDao.deleteModule(gitInfo, module);
    }

    for (Module module : Sets.difference(modules, existing)) {
      buildDefinitionDao.insertModule(gitInfo, module);
    }
  }
}
