package com.hubspot.blazar.data.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.transaction.Transactional;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.dao.ModuleDao;

public class ModuleService {
  private final ModuleDao moduleDao;
  private final DependenciesService dependenciesService;

  @Inject
  public ModuleService(ModuleDao moduleDao, DependenciesService dependenciesService) {
    this.moduleDao = moduleDao;
    this.dependenciesService = dependenciesService;
  }

  public Optional<Module> get(int moduleId) {
    return moduleDao.get(moduleId);
  }

  public Set<Module> getByBranch(int branchId) {
    return moduleDao.getByBranch(branchId);
  }

  public int getBranchIdFromModuleId(int moduleId) {
    return moduleDao.getBranchIdFromModuleId(moduleId);
  }

  @Transactional
  public Set<Module> setModules(GitInfo gitInfo, Set<DiscoveredModule> updatedModules) {
    Set<Module> oldModules = getByBranch(gitInfo.getId().get());
    Set<Module> newModules = new HashSet<>();

    Map<ModuleKey, DiscoveredModule> updatedModulesByName = mapByName(updatedModules);
    Map<ModuleKey, Module> oldModulesByName = mapByName(oldModules);

    for (ModuleKey deletedModule : Sets.difference(oldModulesByName.keySet(), updatedModulesByName.keySet())) {
      Module module = oldModulesByName.get(deletedModule);
      checkAffectedRowCount(moduleDao.delete(module.getId().get()));
      dependenciesService.delete(module.getId().get());
    }

    for (ModuleKey updatedModule : Sets.intersection(oldModulesByName.keySet(), updatedModulesByName.keySet())) {
      Module old = oldModulesByName.get(updatedModule);
      DiscoveredModule updated = updatedModulesByName.get(updatedModule).withId(old.getId().get());
      if (!old.equals(updated)) {
        checkAffectedRowCount(moduleDao.update(updated));
      }
      dependenciesService.update(updated);
      newModules.add(updated);
    }

    for (ModuleKey addedModule : Sets.difference(updatedModulesByName.keySet(), oldModulesByName.keySet())) {
      DiscoveredModule added = updatedModulesByName.get(addedModule);
      int id = moduleDao.insert(gitInfo.getId().get(), added);
      added = added.withId(id);
      dependenciesService.insert(added);
      newModules.add(added);
    }

    return newModules;
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }

  private static <T extends Module> Map<ModuleKey, T> mapByName(Set<T> modules) {
    Map<ModuleKey, T> modulesByName = new HashMap<>();
    for (T module : modules) {
      modulesByName.put(new ModuleKey(module.getName().toLowerCase(), module.getType()), module);
    }

    return modulesByName;
  }

  private static class ModuleKey {
    private final String name;
    private final String type;

    public ModuleKey(String name, String type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ModuleKey moduleKey = (ModuleKey) o;
      return Objects.equals(name, moduleKey.name) && Objects.equals(type, moduleKey.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, type);
    }
  }
}
