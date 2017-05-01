package com.hubspot.blazar.discovery;

import static com.hubspot.blazar.util.ModuleDiscoveryValidations.getDuplicateModules;
import static com.hubspot.blazar.util.ModuleDiscoveryValidations.getDuplicateModulesMalformedFile;
import static com.hubspot.blazar.util.ModuleDiscoveryValidations.preDiscoveryBranchValidation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

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

@Singleton
public class CompositeModuleDiscovery implements ModuleDiscovery {
  private final Set<ModuleDiscovery> delegates;
  private final BuildConfigDiscovery buildConfigDiscovery;
  private final BuildConfigurationResolver buildConfigurationResolver;

  @Inject
  public CompositeModuleDiscovery(Set<ModuleDiscovery> delegates,
                                  BuildConfigDiscovery buildConfigDiscovery,
                                  BuildConfigurationResolver buildConfigurationResolver) {
    this.delegates = delegates;
    this.buildConfigDiscovery = buildConfigDiscovery;
    this.buildConfigurationResolver = buildConfigurationResolver;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (ModuleDiscovery delegate : delegates) {
      if (delegate.isEnabled(gitInfo) && delegate.shouldRediscover(gitInfo, commitInfo)) {
        return true;
      }
    }

    return buildConfigDiscovery.shouldRediscover(gitInfo, commitInfo);
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    Multimap<String, DiscoveredModule> modulesByPath = ArrayListMultimap.create();
    Set<MalformedFile> malformedFiles = new HashSet<>();

    // check if branch name is malformed
    Optional<MalformedFile> malformedBranchFile = preDiscoveryBranchValidation(gitInfo);
    if (malformedBranchFile.isPresent()) {
      return new DiscoveryResult(ImmutableSet.of(), ImmutableSet.of(malformedBranchFile.get()));
    }

    // apply the available plugins to discover modules inside the branch
    applyModuleDiscoveryPlugins(gitInfo, modulesByPath, malformedFiles);

    // check if the discovered modules use the same module name
    checkForDuplicateModuleNames(gitInfo, modulesByPath, malformedFiles);

    // Resolve the build configuration for each discovered module.
    // It is possible that there is no plugin available and users directly specify building instructions
    // in .blazar.yaml files.
    // Another option is that users use .blazar.yaml files to disable module building or override
    // the auto-discovered configurations
    buildConfigurationResolver.findAndResolveBuildConfigurations(gitInfo, modulesByPath, malformedFiles);

    return new DiscoveryResult(ImmutableSet.copyOf(modulesByPath.values()), malformedFiles);
  }

  /**
   * Composite discovery should be run for all modules, the delegates will figure out if they should be run.
   */
  @Override
  public boolean isEnabled(GitInfo gitInfo) {
    return true;
  }

  /*
  * check if we have duplicate names in the auto-discovered modules
  * If we find any we will remove them from the list of discovered modules and will create a MalformedFile instance
  * to surface the error to the UI
  */
  private void checkForDuplicateModuleNames(GitInfo gitInfo, Multimap<String, DiscoveredModule> modulesByPath, Set<MalformedFile> malformedFiles) {
    List<DiscoveredModule> discoveredModules = ImmutableList.copyOf(modulesByPath.values());
    List<DiscoveredModule> duplicateModules = getDuplicateModules(discoveredModules);
    if (!duplicateModules.isEmpty()) {
      duplicateModules.forEach(discoveredModule -> modulesByPath.remove(discoveredModule.getFolder(), discoveredModule));
      MalformedFile duplicateModulesMalFormedFile = getDuplicateModulesMalformedFile(gitInfo, duplicateModules);
      malformedFiles.add(duplicateModulesMalFormedFile);
    }
  }

  /*
  * use the discovery plugins to discover modules
  */
  private void applyModuleDiscoveryPlugins(GitInfo gitInfo, Multimap<String, DiscoveredModule> modulesByPath, Set<MalformedFile> malformedFiles) throws IOException {
    for (ModuleDiscovery delegate : delegates.stream().filter(moduleDiscovery -> moduleDiscovery.isEnabled(gitInfo)).collect(Collectors.toList())) {
      DiscoveryResult result = delegate.discover(gitInfo);
      malformedFiles.addAll(result.getMalformedFiles());
      for (DiscoveredModule module : result.getModules()) {
        modulesByPath.put(module.getFolder(), module);
      }
    }
  }

}
