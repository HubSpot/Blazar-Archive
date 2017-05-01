package com.hubspot.blazar.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildConfigDiscoveryResult;
import com.hubspot.blazar.base.Dependency;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredBuildConfig;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.util.BuildConfigUtils;

@Singleton
public class BuildConfigurationResolver {

  private final BuildConfigDiscovery buildConfigDiscovery;
  private final BuildConfigUtils buildConfigUtils;

  @Inject
  public BuildConfigurationResolver(BuildConfigDiscovery buildConfigDiscovery, BuildConfigUtils buildConfigUtils) {
    this.buildConfigDiscovery = buildConfigDiscovery;
    this.buildConfigUtils = buildConfigUtils;
  }

  /**
   * Here we determine what build configuration will be used for each module examining the provided buildpack in
   * auto-discovered modules and looking if extra build configs exist in the module folders.
   * Folders may contain .blazar.yaml configs besides the other config files that are used for auto-discovery
   * like .pom, package.json, etc.
   * If we discover a .blazar.yaml file inside a folder the following cases exist:
   * a) No other module was automatically discovered by the plugins that we used above:
   *      In this case the config designates a module that gets its name from the folder name we discovered it in.
   *      It is expected that the config either specifies all necessary build steps or points to another
   *      build configuration file that implements the build steps (i.e a reusable buildpack that is maintained
   *      in another repository and shared among modules we want to build)
   * b) One module has been auto-discovered by the plugins:
   *      - if the .blazar.yaml file specifies "enabled: false" then we will remove the auto-discovered module
   *        and we will not add the .blazar.yaml file as a module
   *      - if both .blazar.yaml and the auto-discovered module specify a buildpack then the buildpack in the
   *        auto-discovered module will be ignored when we merge the two configs.
   *        if the .blazar.yaml file doesn't specify a buildpack we use the buildpack from the discovered modules.
   *        In both cases we will register/use the discovered dependencies in the auto-discovered module unless
   *        the merged config contains "ignoreAutoDiscoveredDependencies: true"
   *  c) more than one module is auto-discovered inside a single folder:
   *      - if the .blazar.yaml file specifies "enabled: false" then we will remove the auto-discovered modules
   *        and we will not add the .blazar.yaml file as a module.
   *      - In all other cases except the above it is an error to auto-discover many modules inside a folder
   *        and also find .blazar.yaml file. In other words we allow the .blazar.yaml file only if it disables the
   *        the modules discovery. To clarify more multiple modules alone without a .blazar.yaml file inside a folder
   *        are fine but having a .blazar.yaml file creates issues because it is ambiguous how to apply/merge the
   *        config file with config files of the auto-discovered modules.
   */
  public void findAndResolveBuildConfigurations(GitInfo branch, Multimap<String,
      DiscoveredModule> modulesByPath, Set<MalformedFile> malformedFiles) throws IOException {

    BuildConfigDiscoveryResult buildConfigDiscoveryResult = buildConfigDiscovery.discover(branch);
    malformedFiles.addAll(buildConfigDiscoveryResult.getMalformedFiles());

    for (DiscoveredBuildConfig discoveredBuildConfig : buildConfigDiscoveryResult.getDiscoveredBuildConfigs()) {
      String folder = discoveredBuildConfig.getFolder();

      // check if the discovered config is disabled and disable all auto-discovered modules in this folder
      if (discoveredBuildConfig.getBuildConfig().isDisabled()) {
        modulesByPath.removeAll(folder);
        continue;
      }

      // check what modules we auto-discovered in this folder
      Collection<DiscoveredModule> autoDiscoveredModulesInFolder = modulesByPath.get(folder);

      // no module was auto-discovered so this config will be the basis for a build module
      if (autoDiscoveredModulesInFolder.isEmpty()) {
        createModuleFromBuildConfig(branch, modulesByPath, malformedFiles, discoveredBuildConfig);
      } else if (autoDiscoveredModulesInFolder.size() == 1) { // one module was auto-discovered
        createModuleFromBuildConfigAndAutoDiscoveredDependencies(branch, modulesByPath, malformedFiles,
            discoveredBuildConfig, autoDiscoveredModulesInFolder.iterator().next());
      } else { // more than one auto-discovered modules
        // it is an error to have more than one auto-discovered modules and a build config in the folder
        // because we won't know how to combine the build config with the different auto-discovered modules
        modulesByPath.removeAll(folder);
        String message = String.format("Build configuration %s coexists with more that one auto-discovered modules (%s). " +
            "Only a single discovered module can exist within a folder that contains a build configuration file, " +
            "otherwise it is not known how to apply the build configuration to each of the discovered modules",
            discoveredBuildConfig.getPath(), autoDiscoveredModulesInFolder.stream().map(DiscoveredModule::getName).collect(Collectors.joining(" ,")));
        malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-build-config-combination-with-multiple-discovered-modules",
            discoveredBuildConfig.getPath(), message));
      }
    }

    // We have resolved the build configurations for all folders that contain build configs
    // We also need to resolve build configurations for the modules that were auto-discovered in forlders without
    // explicit build configs. In that case the build config will be determined by the buildpack that is specified in
    // the discovered module.
    for (Entry<String, DiscoveredModule> entry : modulesByPath.entries()) {
      DiscoveredModule discoveredModule = entry.getValue();
      if (!discoveredModule.getBuildpack().isPresent()) {
        String message = String.format("The discovered module %s (type: %s) in path %s doesn't specify any buildpack " +
            "and no other build configuration was found in the folder. Please update the relevant discovery plugin " +
            "to specify a buildpack", discoveredModule.getName(), discoveredModule.getType(), discoveredModule.getPath());
        malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-or-nonexistent-buildpack",
            discoveredModule.getPath(), message));
        modulesByPath.remove(entry.getKey(), entry.getValue());
        continue;
      }
      if (!discoveredModule.getBuildConfig().isPresent()) {

        try {
          BuildConfig buildpackBuildConfig = buildConfigUtils.getConfigForBuildpackOnBranch(discoveredModule.getBuildpack().get());
          BuildConfig resolvedBuildConfig = buildConfigUtils.addMissingBuildConfigSettings(buildpackBuildConfig);
          // remove and re-add the entry adding the original and the resolved build config.
          modulesByPath.remove(entry.getKey(), entry.getValue());
          DiscoveredModule updatedDiscoveredModule = new DiscoveredModule(
              discoveredModule.getName(),
              discoveredModule.getType(),
              discoveredModule.getPath(),
              discoveredModule.getGlob(),
              discoveredModule.getBuildpack(),
              discoveredModule.getDependencyInfo(),
              Optional.of(buildpackBuildConfig),
              Optional.of(resolvedBuildConfig));
          modulesByPath.put(entry.getKey(), updatedDiscoveredModule);
        } catch (Exception e) {
          malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-or-nonexistent-buildpack", discoveredModule.getPath(), e.getMessage()));
          modulesByPath.remove(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private void createModuleFromBuildConfigAndAutoDiscoveredDependencies(GitInfo branch,
                                                                   Multimap<String, DiscoveredModule> modulesByPath,
                                                                   Set<MalformedFile> malformedFiles,
                                                                   DiscoveredBuildConfig discoveredBuildConfig,
                                                                   DiscoveredModule autoDiscoveredModule) {

    BuildConfig buildConfig = discoveredBuildConfig.getBuildConfig();
    String buildConfigFolder = discoveredBuildConfig.getFolder();

    BuildConfig resolvedBuildConfig;
    if (buildConfig.getBuildpack().isPresent()) { // We will ignore the buildpack specified in the discovered module
      Optional<BuildConfig> resolvedBuildConfigOptional = getResolvedBuildConfig(buildConfig, malformedFiles, branch);

      if (!resolvedBuildConfigOptional.isPresent()) {
        return;
      }
      resolvedBuildConfig = resolvedBuildConfigOptional.get();
    } else if (autoDiscoveredModule.getBuildpack().isPresent()){
      Optional<BuildConfig> resolvedBuildConfigOptional =
          getResolvedBuildConfigFromPrimaryBuildConfigAndBuildPack(buildConfig, autoDiscoveredModule.getBuildpack().get(), malformedFiles, branch);
      if (!resolvedBuildConfigOptional.isPresent()) {
        return;
      }
      resolvedBuildConfig = resolvedBuildConfigOptional.get();
    } else {
      resolvedBuildConfig = buildConfig;
    }

    if (!checkCanBuild(discoveredBuildConfig, resolvedBuildConfig, branch, malformedFiles)) {
      return;
    }

    // We want to merge the auto-discovered dependencies unless the build config specifies
    // ignoreAutoDiscoveredDependencies = true
    Set<Dependency> moduleDepencies = buildConfig.getDepends();
    Set<Dependency> moduleProvidedDependencies = buildConfig.getProvides();
    if (!resolvedBuildConfig.isIgnoreAutoDiscoveredDependencies()) {
      moduleDepencies.addAll(autoDiscoveredModule.getDependencyInfo().getDepends());
      moduleProvidedDependencies.addAll(autoDiscoveredModule.getDependencyInfo().getProvides());
    }

    String moduleName = moduleName(branch, discoveredBuildConfig.getFolder());
    modulesByPath.put(buildConfigFolder,
        new DiscoveredModule(
            moduleName,
            "config",
            discoveredBuildConfig.getPath(),
            discoveredBuildConfig.getGlob(),
            buildConfig.getBuildpack(),
            new DependencyInfo(moduleDepencies, moduleProvidedDependencies),
            Optional.of(buildConfig),
            Optional.of(resolvedBuildConfig)));
  }

  private void createModuleFromBuildConfig(GitInfo branch,
                                   Multimap<String, DiscoveredModule> modulesByPath,
                                   Set<MalformedFile> malformedFiles,
                                   DiscoveredBuildConfig discoveredBuildConfig) {

    BuildConfig buildConfig = discoveredBuildConfig.getBuildConfig();
    String buildConfigFolder = discoveredBuildConfig.getFolder();

    Optional<BuildConfig> resolvedBuildConfigOptional = getResolvedBuildConfig(buildConfig, malformedFiles, branch);

    if (!resolvedBuildConfigOptional.isPresent()) {
      return;
    }

    BuildConfig resolvedBuildConfig = resolvedBuildConfigOptional.get();

    if (!checkCanBuild(discoveredBuildConfig, resolvedBuildConfig, branch, malformedFiles)) {
      return;
    }

    String moduleName = moduleName(branch, discoveredBuildConfig.getFolder());
    modulesByPath.put(buildConfigFolder,
        new DiscoveredModule(
            moduleName,
            "config",
            discoveredBuildConfig.getPath(),
            discoveredBuildConfig.getGlob(),
            buildConfig.getBuildpack(),
            new DependencyInfo(buildConfig.getDepends(), buildConfig.getProvides()),
            Optional.of(buildConfig),
            Optional.of(resolvedBuildConfig)));


  }

  // If the build config specifies a buildpack we will get it and merge it. We also want to fill in required config
  // values with defaults
  private Optional<BuildConfig> getResolvedBuildConfig(BuildConfig buildConfig, Set<MalformedFile> malformedFiles, GitInfo branch) {
    if (buildConfig.getBuildpack().isPresent()) {
     return getResolvedBuildConfigFromPrimaryBuildConfigAndBuildPack(buildConfig, buildConfig.getBuildpack().get(), malformedFiles, branch);
    } else {
      return Optional.of(buildConfigUtils.addMissingBuildConfigSettings(buildConfig));
    }
  }

  // If the build config specifies a buildpack we will get it and merge it. We also want to fill in required config
  // values with defaults
  private Optional<BuildConfig> getResolvedBuildConfigFromPrimaryBuildConfigAndBuildPack(BuildConfig primaryBuildConfig,
                                                                                         GitInfo buildpackBranchInfo,
                                                                                         Set<MalformedFile> malformedFiles,
                                                                                         GitInfo branch) {
    BuildConfig buildpackBuildConfig;
    try {
      buildpackBuildConfig = buildConfigUtils.getConfigForBuildpackOnBranch(buildpackBranchInfo);
    } catch (Exception e) {
      malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-or-nonexistent-buildpack", "/", e.getMessage()));
      return Optional.absent();
    }
    return Optional.of(buildConfigUtils.addMissingBuildConfigSettings(buildConfigUtils.mergeBuildConfigs(primaryBuildConfig, buildpackBuildConfig)));
  }

  private boolean checkCanBuild(DiscoveredBuildConfig discoveredBuildConfig, BuildConfig resolvedBuildConfig, GitInfo branch, Set<MalformedFile> malformedFiles) {
    if (resolvedBuildConfig.getSteps().isEmpty()) {
      String message = String.format("Build config in path %s is incomplete, i.e. the resolved build config specifies no build steps.",
          discoveredBuildConfig.getPath());
      malformedFiles.add(new MalformedFile(branch.getId().get(), "incomplete-build-module", "/", message));
      return false;
    }
    return true;
  }

  private static String moduleName(GitInfo gitInfo, String buildConfigFolder) {
    return buildConfigFolder.isEmpty() ? gitInfo.getRepository() : folderName(buildConfigFolder);
  }

  private static String folderName(String buildConfigFolder) {
    return buildConfigFolder.contains("/") ? buildConfigFolder.substring(buildConfigFolder.lastIndexOf('/') + 1) : buildConfigFolder;
  }
}
