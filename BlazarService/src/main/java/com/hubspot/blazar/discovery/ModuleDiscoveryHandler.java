package com.hubspot.blazar.discovery;

import static com.hubspot.blazar.util.ModuleDiscoveryValidations.getDuplicateModules;
import static com.hubspot.blazar.util.ModuleDiscoveryValidations.getDuplicateModulesMalformedFile;
import static com.hubspot.blazar.util.ModuleDiscoveryValidations.preDiscoveryBranchValidation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleDiscoveryResult;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.data.service.ModuleService;

@Singleton
public class ModuleDiscoveryHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleDiscoveryHandler.class);

  private final Set<ModuleDiscovery> moduleDiscoveryPlugins;
  private final BuildConfigDiscovery buildConfigDiscovery;
  private final BuildConfigurationResolver buildConfigurationResolver;
  private final ModuleDiscoveryService moduleDiscoveryService;
  private final ModuleService moduleService;
  private final DependenciesService dependenciesService;

  @Inject
  public ModuleDiscoveryHandler(Set<ModuleDiscovery> moduleDiscoveryPlugins,
                                BuildConfigDiscovery buildConfigDiscovery,
                                BuildConfigurationResolver buildConfigurationResolver,
                                ModuleDiscoveryService moduleDiscoveryService,
                                ModuleService moduleService,
                                DependenciesService dependenciesService) {

    this.moduleDiscoveryPlugins = moduleDiscoveryPlugins;
    this.buildConfigDiscovery = buildConfigDiscovery;
    this.buildConfigurationResolver = buildConfigurationResolver;
    this.moduleDiscoveryService = moduleDiscoveryService;
    this.moduleService = moduleService;
    this.dependenciesService = dependenciesService;
  }

  public ModuleDiscoveryResult updateModules(GitInfo branch, boolean persistUpdatedModules) throws IOException {
    return updateModules(branch, Optional.absent(), persistUpdatedModules);
  }

  public ModuleDiscoveryResult updateModules(GitInfo branch, CommitInfo commitInfo, boolean persistUpdatedModules) throws IOException {
    return updateModules(branch, Optional.of(commitInfo), persistUpdatedModules);
  }

  private ModuleDiscoveryResult updateModules(GitInfo branch, Optional<CommitInfo> commitInfo, boolean persistUpdatedModules) throws IOException {
    Multimap<String, Module> discoveredModulesByFolder = ArrayListMultimap.create();
    Set<MalformedFile> malformedFiles = new HashSet<>();

    // check if branch name is malformed
    Optional<MalformedFile> malformedBranchFile = preDiscoveryBranchValidation(branch);
    if (malformedBranchFile.isPresent()) {
      ModuleDiscoveryResult moduleDiscoveryResult = new ModuleDiscoveryResult(ImmutableSet.of(), ImmutableSet.of(malformedBranchFile.get()));
      if (persistUpdatedModules) {
        moduleDiscoveryService.persistDiscoveryResult(branch, moduleDiscoveryResult);
      }
      return moduleDiscoveryResult;
    }

    // apply the available plugins to discover modules inside the branch
    applyModuleDiscoveryPlugins(branch, commitInfo, discoveredModulesByFolder, malformedFiles);

    // We will now create a combined map of the (re)discovered modules plus the modules that have been registered before
    // and have not been deleted during this discovery iteration.
    Multimap<String, Module> allActiveModules = ArrayListMultimap.create(discoveredModulesByFolder);
    Set<Module> alreadyRegisteredModules = moduleService.getByBranch(branch.getId().get());
    Set<String> rediscoveredModuleTypes = discoveredModulesByFolder.values().stream().map(Module::getType).collect(Collectors.toSet());
    alreadyRegisteredModules.forEach(module -> {
      boolean isRediscovered = isRediscovered(module, discoveredModulesByFolder.get(module.getFolder()));
      // if it was not discovered this time but its type is among the types we have rediscovered then it was deleted
      // and we will not add it in the list
      if (module.isActive() && !isRediscovered && !rediscoveredModuleTypes.contains(module.getType())) {
        allActiveModules.put(module.getFolder(), module);
      }
    });

    // check if modules use the same module name
    checkForDuplicateModuleNames(branch, allActiveModules, malformedFiles);

    // Resolve the build configuration for each active module.
    // It is possible that there is no plugin available and users directly specify building instructions
    // in .blazar.yaml files.
    // Another option is that users use .blazar.yaml files to disable module building or override
    // the auto-discovered configurations
    buildConfigurationResolver.findAndResolveBuildConfigurations(branch, allActiveModules, malformedFiles);

    ModuleDiscoveryResult moduleDiscoveryResult = new ModuleDiscoveryResult(ImmutableSet.copyOf(allActiveModules.values()), ImmutableSet.copyOf(malformedFiles));
    if (persistUpdatedModules) {
      moduleDiscoveryService.persistDiscoveryResult(branch, moduleDiscoveryResult);
    }

    return moduleDiscoveryResult;
  }


  private boolean isRediscovered(Module existingModule, Collection<Module> discoveredModulesInSameFolder) {
    return discoveredModulesInSameFolder.stream().anyMatch(discoveredModuleInSameFolder ->
        discoveredModuleInSameFolder.getName().equals(existingModule) &&
            discoveredModuleInSameFolder.getType().equals(existingModule.getType()));
  }

  /*
  * check if we have duplicate names in the auto-discovered modules
  * If we find any we will remove them from the list of discovered modules and will create a MalformedFile instance
  * to surface the error to the UI
  */
  private void checkForDuplicateModuleNames(GitInfo branch, Multimap<String, Module> modulesByPath, Set<MalformedFile> malformedFiles) {
    List<Module> modules = ImmutableList.copyOf(modulesByPath.values());
    List<Module> duplicateModules = getDuplicateModules(modules);
    if (!duplicateModules.isEmpty()) {
      duplicateModules.forEach(module -> modulesByPath.remove(module.getFolder(), module));
      MalformedFile duplicateModulesMalFormedFile = getDuplicateModulesMalformedFile(branch, modules);
      malformedFiles.add(duplicateModulesMalFormedFile);
    }
  }

  /*
  * use the discovery plugins to discover modules
  */
  private void applyModuleDiscoveryPlugins(GitInfo branch,
                                           Optional<CommitInfo> commitInfo,
                                           Multimap<String, Module> discoveredModulesByFolder,
                                           Set<MalformedFile> malformedFiles) throws IOException {

    // if the dependency source is missing in existing dependency entries we rediscover the modules so the entries will
    // be updated with the source info
    boolean dependencySourceIsMissingInBranchModules =
        dependenciesService.getCountOfDependenciesWithoutSourceByBranchId(branch.getId().get()) > 0 ||
            dependenciesService.getCountOfProvidedDependenciesWithoutSourceByBranchId(branch.getId().get()) > 0;

    String fullBranchName = String.format("%s/%s",branch.getFullRepositoryName(), branch.getBranch());
    LOG.debug("Dependency source is missing for modules in branch {}: {}", fullBranchName, dependencySourceIsMissingInBranchModules);
    LOG.debug("Commit info was provided for module discovery in branch {}: {}", fullBranchName, commitInfo.isPresent());
    LOG.debug("Commit info for branch {} is truncated: {}", fullBranchName, commitInfo.isPresent() && commitInfo.get().isTruncated());
    boolean rediscoverAllModules = !commitInfo.isPresent() || commitInfo.get().isTruncated() || dependencySourceIsMissingInBranchModules;
    LOG.debug("Modules for branch {} will be rediscovered: {}", fullBranchName, rediscoverAllModules);
    Set<ModuleDiscovery> moduleDiscoveryPluginsToUse = new HashSet<>();
    for (ModuleDiscovery moduleDiscoveryPlugin : moduleDiscoveryPlugins) {
      if (moduleDiscoveryPlugin.isEnabled(branch) && (rediscoverAllModules || (commitInfo.isPresent() && moduleDiscoveryPlugin.shouldRediscover(branch, commitInfo.get())))) {
        moduleDiscoveryPluginsToUse.add(moduleDiscoveryPlugin);
      }
    }

    for (ModuleDiscovery moduleDiscoveryPluginToUse : moduleDiscoveryPluginsToUse) {
      DiscoveryResult result = moduleDiscoveryPluginToUse.discover(branch);
      malformedFiles.addAll(result.getMalformedFiles());
      for (DiscoveredModule module : result.getModules()) {
        discoveredModulesByFolder.put(module.getFolder(), module);
      }
    }
  }

}
