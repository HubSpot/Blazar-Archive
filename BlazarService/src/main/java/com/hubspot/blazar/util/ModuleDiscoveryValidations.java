package com.hubspot.blazar.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;

public class ModuleDiscoveryValidations {

  /**
   * Currently we do not support branch names with special characters despite the fact that github allows certain special
   * characters in branch names (e.g. a single quote). Having special characters in the branch name causes problems in
   * services that blazar uses to handle builds, i.e. the build executor and our maven service that detects modules.
   *
   * @param gitInfo The branch we are checking for validity.
   * @return A malformed "file" representing the invalid branch name if it is invalid
   */
  public static Optional<MalformedFile> preDiscoveryBranchValidation(GitInfo gitInfo) {
    String branch = gitInfo.getBranch();
    if (branch.contains("'") ||
        branch.contains("`") ||
        branch.contains("\"") ||
        ! CharMatcher.ASCII.matchesAllOf(branch)) {
      String message = String.format("Branch %s contained non-ascii or quotation characters not supported by Blazar.%nPlease re-create your branch with a new name.", branch);
      return Optional.of(new MalformedFile(gitInfo.getId().get(), "branch-validation", "/", message));
    }

    return Optional.absent();
  }

  /**
   * Check if we have more than one module with the same name inside a branch
   *
   * @param discoveredModules The modules that were discovered inside the branch.
   * @return A malformed "file" representing the duplicate module names if present
   */
  public static List<DiscoveredModule> getDuplicateModules(List<DiscoveredModule> discoveredModules) {
    List<DiscoveredModule> sortedDiscoveredModules = discoveredModules.stream().sorted(Comparator.comparing(DiscoveredModule::getName)).collect(Collectors.toList());
    // We want to add all duplicates if there are more that one duplicates per module so we don't use a Set
    List<DiscoveredModule> duplicateModules = new ArrayList<>();
    int examinedModulePointer = 0;
    int nextModulePointer = 1;
    while (examinedModulePointer < sortedDiscoveredModules.size() - 1 && nextModulePointer < sortedDiscoveredModules.size()) {
      DiscoveredModule examinedModule = sortedDiscoveredModules.get(examinedModulePointer);
      DiscoveredModule nextModule = sortedDiscoveredModules.get(nextModulePointer);
      if(examinedModule.getName().equals(nextModule.getName())) {
        duplicateModules.add(nextModule);
        //if that's the first duplicate we find then we add the examined in the list too
        if (nextModulePointer - examinedModulePointer == 1) {
          duplicateModules.add(examinedModule);
        }
      } else {
        examinedModulePointer = nextModulePointer;
      }
      ++nextModulePointer;
    }

    return ImmutableList.copyOf(duplicateModules);
  }

  public static MalformedFile getDuplicateModulesMalformedFile(GitInfo gitInfo, List<DiscoveredModule> duplicateModules) {
    // group duplicates by name
    ListMultimap<String, DiscoveredModule> duplicateModulesPerModuleName = Multimaps.index(duplicateModules, DiscoveredModule::getName);

    // and turn the map in lines like "duplicateName -> [folderName:moduleType, folderName2:moduleType2, ...]
    String duplicatesEntriesAsString = duplicateModulesPerModuleName.asMap().entrySet().stream().map(entry -> {
      String duplicateModulesInEntry = entry.getValue().stream().map(duplicateModule -> String.format("%s/%s", duplicateModule.getFolder(), duplicateModule.getType())).collect(Collectors.joining(" ,"));
      return String.format("%s: [%s]%n", duplicateModulesInEntry);
    }).collect(Collectors.joining(" ,"));

    String message = String.format("The following discovered module(s) share the same module name (duplicateName -> [folderName:moduleType, folderName2:moduleType2, ...]: %s. Module names should be unique inside each repository branch.",
        duplicatesEntriesAsString);
    return new MalformedFile(gitInfo.getId().get(), "duplicate-module-validation", "/", message);
  }

}
