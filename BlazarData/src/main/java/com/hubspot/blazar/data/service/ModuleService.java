package com.hubspot.blazar.data.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
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
  public Set<Module> setModules(GitInfo gitInfo, Set<Module> updatedModules) {
    Set<Module> oldModules = getModules(gitInfo);
    Set<Module> newModules = new HashSet<>();

    Map<String, Module> updatedModulesByName = mapByName(updatedModules);
    Map<String, Module> oldModulesByName = mapByName(oldModules);

    for (String deletedModule : Sets.difference(oldModulesByName.keySet(), updatedModulesByName.keySet())) {
      checkAffectedRowCount(moduleDao.delete(oldModulesByName.get(deletedModule).getId().get()));
    }

    for (String updatedModule : Sets.intersection(oldModulesByName.keySet(), updatedModulesByName.keySet())) {
      long id = oldModulesByName.get(updatedModule).getId().get();
      Module updated = updatedModulesByName.get(updatedModule).withId(id);
      checkAffectedRowCount(moduleDao.update(updated));
      newModules.add(updated);
    }

    for (String addedModule : Sets.intersection(updatedModulesByName.keySet(), oldModulesByName.keySet())) {
      Module added = updatedModulesByName.get(addedModule);
      long id = moduleDao.insert(gitInfo.getId().get(), added);
      newModules.add(added.withId(id));
    }

    return newModules;
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }

  private static Map<String, Module> mapByName(Set<Module> modules) {
    Map<String, Module> modulesByName = new HashMap<>();
    for (Module module : modules) {
      modulesByName.put(module.getName(), module);
    }

    return modulesByName;
  }
}
