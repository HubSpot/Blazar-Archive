package com.hubspot.blazar.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;

@Singleton
public class CompositeModuleDiscovery implements ModuleDiscovery {
  private final Set<ModuleDiscovery> delegates;
  private final BlazarConfigModuleDiscovery configDiscovery;

  @Inject
  public CompositeModuleDiscovery(Set<ModuleDiscovery> delegates, BlazarConfigModuleDiscovery configDiscovery) {
    this.delegates = delegates;
    this.configDiscovery = configDiscovery;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (ModuleDiscovery delegate : delegates) {
      if (delegate.isEnabled(gitInfo) && delegate.shouldRediscover(gitInfo, commitInfo)) {
        return true;
      }
    }

    return configDiscovery.shouldRediscover(gitInfo, commitInfo);
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    Map<String, Set<DiscoveredModule>> modulesByPath = new HashMap<>();
    Set<MalformedFile> malformedFiles = new HashSet<>();

    Optional<MalformedFile> malformedBranchFile = preDiscoveryBranchValidation(gitInfo);
    if (malformedBranchFile.isPresent()) {
      return new DiscoveryResult(ImmutableSet.of(), ImmutableSet.of(malformedBranchFile.get()));
    }

    for (ModuleDiscovery delegate : delegates.stream().filter(moduleDiscovery -> moduleDiscovery.isEnabled(gitInfo)).collect(Collectors.toList())) {
      DiscoveryResult result = delegate.discover(gitInfo);
      malformedFiles.addAll(result.getMalformedFiles());
      for (DiscoveredModule module : result.getModules()) {
        String folder = module.getFolder();

        Set<DiscoveredModule> modules = modulesByPath.get(folder);
        if (modules == null) {
          modules = new HashSet<>();
          modulesByPath.put(folder, modules);
        }

        modules.add(module);
      }
    }

    DiscoveryResult result = configDiscovery.discover(gitInfo);
    malformedFiles.addAll(result.getMalformedFiles());
    for (DiscoveredModule module : result.getModules()) {
      String folder = module.getFolder();

      if (!module.isActive()) {
        modulesByPath.remove(folder);
      } else if (!modulesByPath.containsKey(folder)) {
        modulesByPath.put(folder, ImmutableSet.of(module));
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (Set<DiscoveredModule> folderModules : modulesByPath.values()) {
      modules.addAll(folderModules);
    }

    return new DiscoveryResult(modules, malformedFiles);
  }

  /**
   * Composite discovery should be run for all modules, the delegates will figure out if they should be run.
   */
  @Override
  public boolean isEnabled(GitInfo gitInfo) {
    return true;
  }

  /**
   * Currently we do not support branch names with special characters despite the fact that github allows certain special
   * characters in branch names (e.g. a single quote). Having special characters in the branch name causes problems in
   * services that blazar uses to handle builds, i.e. the build executor and our maven service that detects modules.
   *
   * @param gitInfo The branch we are checking for validity.
   * @return A malformed "file" representing the invalid branch name if it is invalid
   */
  private Optional<MalformedFile> preDiscoveryBranchValidation(GitInfo gitInfo) {
    String branch = gitInfo.getBranch();
    if (branch.contains("'") ||
        branch.contains("`") ||
        branch.contains("\"") ||
        ! CharMatcher.ASCII.matchesAllOf(branch)) {
      String message = String.format("Branch %s contained non-ascii or quotation characters not supported by Blazar.\nPlease re-create your branch with a new name.", branch);
      return Optional.of(new MalformedFile(gitInfo.getId().get(), "branch-validation", "/", message));
    }

    return Optional.absent();
  }
}
