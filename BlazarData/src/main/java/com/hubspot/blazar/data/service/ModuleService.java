package com.hubspot.blazar.data.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.dao.ModuleDao;

public class ModuleService {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleService.class);
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

  public void checkModuleExists(int moduleId) {
    Optional<Module> maybeModule = get(moduleId);
    if (!maybeModule.isPresent()) {
      throw new NotFoundException(String.format("Module %d does not exist", moduleId));
    }
  }

  /**
   * The updated modules are all the active modules that have been updated during the module discovery phase.
   * The list contains all modules that were (re)discovered (they are in a DiscoveredModule
   * subclass of the Module class) + all the registered modules that were not deactivated or
   * deleted during the discovery check (they are in a Module class, for those we just updated their build configs).
   * Essentially the list of updated modules is the list of all modules that are
   * eligible for building. We use the list to identify which modules were deleted/deactivated in order to deactivate
   * them in the db and then we persist the changes in each updated module. The DiscoveredModule entries in the list
   * may have updated build configs and updated dependencies. The Module entries have only their build configs updated.
   * @param branch
   * @param updatedModules
   * @return
   */
  @Transactional
  public void persistModulesAndDependencies(GitInfo branch, Set<Module> updatedModules) {
    Set<Module> registeredActiveModules = getByBranch(branch.getId().get()).stream().filter(Module::isActive).collect(Collectors.toSet());

    Map<String, Module> updatedModulesByName = Maps.uniqueIndex(updatedModules, Module::getName);
    Map<String, Module> registeredActiveModulesByName = Maps.uniqueIndex(registeredActiveModules, Module::getName);
    LOG.debug("Registered Active Modules: {}", registeredActiveModulesByName.toString());
    LOG.debug("Updated Modules: {}", updatedModulesByName.toString());

    for (String deletedModule : Sets.difference(registeredActiveModulesByName.keySet(), updatedModulesByName.keySet())) {
      Module module = registeredActiveModulesByName.get(deletedModule);
      checkAffectedRowCount(moduleDao.deactivate(module.getId().get()));
      dependenciesService.delete(module.getId().get());
    }

    // For already registered modules that were not rediscovered we will just update the module in db if the
    // build config has been refreshed
    Set<Module> alreadyRegisteredAndNotRediscovedModules = updatedModules.stream()
        .filter(module -> module.getClass() == Module.class).collect(Collectors.toSet());

    alreadyRegisteredAndNotRediscovedModules.forEach(existingModule -> {
      Module previousModuleInstance = registeredActiveModulesByName.get(existingModule.getName());
      if (!previousModuleInstance.equals(existingModule)) {
        LOG.debug("Existing module {}(type:{}, id:{}) has a changed build config. We will persist it.", existingModule.getName(), existingModule.getType(), existingModule.getId().get());
        checkAffectedRowCount(moduleDao.update(existingModule));
      } else {
        LOG.debug("Existing module {}(type:{}, id:{}) has no changes in its build config, will not persist it", existingModule.getName(), existingModule.getType(), existingModule.getId().get());
      }
    });

    // For newly discovered modules we will create module entries and will also create entries in the dependencies
    // tables.
    Set<Module> newlyDiscoveredModules = updatedModules.stream()
        .filter(module -> module.getClass() == DiscoveredModule.class && !registeredActiveModulesByName.containsKey(module.getName())).collect(Collectors.toSet());
    newlyDiscoveredModules.forEach(newModule -> {
      LOG.debug("Persisting newly discovered module {}(type:{})", newModule.getName(), newModule.getType());
      int moduleId = moduleDao.insert(branch.getId().get(), newModule);
      LOG.debug("Persisted newly discovered module {}:{} with id:{})", newModule.getName(), newModule.getType(), moduleId);
      LOG.debug("Persisting dependencies for newly discovered module {}(type:{}, id:{})", newModule.getName(), newModule.getType(), moduleId);
      DiscoveredModule persistedModule = ((DiscoveredModule) newModule).withId(moduleId);
      dependenciesService.insert(persistedModule);
    });

    // For re-discovered modules we will update their module entries in db if the
    // build config has been refreshed and also update the relevant entries in the dependencies
    // tables.
    Set<Module> rediscoveredModules = updatedModules.stream()
        .filter(module -> module.getClass() == DiscoveredModule.class && registeredActiveModulesByName.containsKey(module.getName())).collect(Collectors.toSet());
    rediscoveredModules.forEach(rediscoveredModule -> {

      Module previousModuleInstance = registeredActiveModulesByName.get(rediscoveredModule.getName());

      int moduleId = previousModuleInstance.getId().get();

      DiscoveredModule rediscoveredModuleWithId = ((DiscoveredModule)rediscoveredModule).withId(moduleId);

      if (buildConfigChanged(previousModuleInstance, rediscoveredModule)) {
        LOG.debug("Rediscovered module {}(type:{}, id:{}) has a changed build config. We will persist it.", rediscoveredModule.getName(), rediscoveredModule.getType(), moduleId);
        checkAffectedRowCount(moduleDao.update(rediscoveredModuleWithId));
      } else {
        LOG.debug("Rediscovered module {}(type:{}, id:{}) has no changes in its build config, will not persist it", rediscoveredModule.getName(), rediscoveredModule.getType(), moduleId);
      }

      LOG.debug("Persisting dependencies for rediscovered module {}(type:{}, id:{})", rediscoveredModule.getName(), rediscoveredModule.getType(), moduleId);
      dependenciesService.update(rediscoveredModuleWithId);
    });
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }

  private boolean buildConfigChanged(Module previousModuleInstance, Module rediscoveredModule) {
    return !previousModuleInstance.getBuildConfig().equals(rediscoveredModule.getBuildConfig()) ||
        !previousModuleInstance.getResolvedBuildConfig().equals(rediscoveredModule.getResolvedBuildConfig());
  }

}
