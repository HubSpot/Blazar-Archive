package com.hubspot.blazar.data.service;

import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.dao.ModuleDao;
import com.hubspot.guice.transactional.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleService {
  private final ModuleDao moduleDao;

  @Inject
  public ModuleService(ModuleDao moduleDao) {
    this.moduleDao = moduleDao;
  }

  public Set<Module> getModules(GitInfo gitInfo) {
    return moduleDao.getByBranch(gitInfo.getId().get());
  }

  @Transactional
  public Set<Module> setModules(GitInfo gitInfo, Set<Module> modules) {
    Map<String, Module> modulesByName = mapByName(modules);
    Set<Module> oldModules = getModules(gitInfo);

    for (Module oldModule : oldModules) {
      if (!modulesByName.containsKey(oldModule.getName())) {
        moduleDao.delete(oldModule.getId().get());
      }
    }

    Set<Module> newModules = new HashSet<>();
    for (Module module : modules) {
      long id = moduleDao.upsert(gitInfo.getId().get(), module);
      newModules.add(module.withId(id));
    }

    return newModules;
  }

  private static Map<String, Module> mapByName(Set<Module> modules) {
    Map<String, Module> modulesByName = new HashMap<>();
    for (Module module : modules) {
      modulesByName.put(module.getName(), module);
    }

    return modulesByName;
  }
}
