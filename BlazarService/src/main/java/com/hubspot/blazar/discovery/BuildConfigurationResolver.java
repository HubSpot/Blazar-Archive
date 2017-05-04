package com.hubspot.blazar.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.util.BuildConfigUtils;

@Singleton
public class BuildConfigurationResolver {

  public static final String MODULE_TYPE_OF_BUILD_CONFIG = "config";

  private final BuildConfigDiscovery buildConfigDiscovery;
  private final BuildConfigUtils buildConfigUtils;

  @Inject
  public BuildConfigurationResolver(BuildConfigDiscovery buildConfigDiscovery, BuildConfigUtils buildConfigUtils) {
    this.buildConfigDiscovery = buildConfigDiscovery;
    this.buildConfigUtils = buildConfigUtils;
  }

  /**
   * Here we determine what build configuration will be used for each module examining the provided buildpack in
   * modules and looking if extra build configs exist in the module folders.
   * Folders may contain .blazar.yaml configs besides the other config files that are used for auto-discovery
   * like .pom, package.json, etc.
   * If we discover a .blazar.yaml file inside a folder the following cases exist:
   * a) No other module was automatically discovered by the plugins:
   *      In this case the config designates a module (of type MODULE_TYPE_OF_BUILD_CONFIG) that gets its name
   *      from the folder name we discovered it in.
   *      It is expected that the config either specifies all necessary build steps or points to another
   *      build configuration file that implements the build steps (i.e a reusable buildpack that is maintained
   *      in another repository and shared among modules we want to build)
   * b) One module has been discovered by the plugins:
   *      - if the .blazar.yaml file specifies "enabled: false" then we will remove the plugin-discovered module
   *        and we will not add the .blazar.yaml file as a module
   *      - if both .blazar.yaml and the auto-discovered module specify a buildpack then the buildpack in the
   *        auto-discovered module will be ignored when we merge the two configs.
   *        if the .blazar.yaml file doesn't specify a buildpack we use the buildpack from the discovered module.
   *        In both cases we will register the discovered dependencies in the plugin-discovered module unless
   *        the build config contains "ignoreAutoDiscoveredDependencies: true"
   *        The plugin-discovered module will be updated with the resolved build config and dependencies.
   *  c) more than one module is auto-discovered inside a single folder:
   *      - if the .blazar.yaml file specifies "enabled: false" then we will remove the auto-discovered modules
   *        and we will not add the .blazar.yaml file as a module.
   *      - In all other cases except the above it is an error to discover many modules inside a folder
   *        and also find .blazar.yaml file. In other words we allow the .blazar.yaml file only if it disables the
   *        the modules discovery. To clarify more, multiple modules alone without a .blazar.yaml file inside a folder
   *        are fine but having a .blazar.yaml file creates issues because it is ambiguous how to apply/merge the
   *        config file with config files of the discovered modules.
   */
  public void findAndResolveBuildConfigurations(GitInfo branch,
                                                Multimap<String, Module> modulesByFolder,
                                                Set<MalformedFile> malformedFiles) throws IOException {

    BuildConfigDiscoveryResult buildConfigDiscoveryResult = buildConfigDiscovery.discover(branch);
    malformedFiles.addAll(buildConfigDiscoveryResult.getMalformedFiles());

    for (DiscoveredBuildConfig discoveredBuildConfig : buildConfigDiscoveryResult.getDiscoveredBuildConfigs()) {
      String folder = discoveredBuildConfig.getFolder();

      // check if the discovered config is disabled and disable all plugin-discovered modules in this folder
      if (discoveredBuildConfig.getBuildConfig().isDisabled()) {
        modulesByFolder.removeAll(folder);
        continue;
      }

      // check what modules we have in this folder
      Collection<Module> modulesInFolder = modulesByFolder.get(folder);
      // We will exclude the modules that have type: MODULE_TYPE_OF_BUILD_CONFIG to keep only the
      // modules that have been discovered by plugins
      Collection<Module> pluginDiscoveredModulesInFolder = modulesInFolder.stream().filter(module ->
          !module.getType().equals(MODULE_TYPE_OF_BUILD_CONFIG)).collect(Collectors.toSet());

      // if no module was discovered by plugins this config will be the basis for a build module
      if (pluginDiscoveredModulesInFolder.isEmpty()) {
        createModuleFromBuildConfig(branch, modulesByFolder, modulesInFolder, malformedFiles, discoveredBuildConfig);
      } else if (pluginDiscoveredModulesInFolder.size() == 1) { // one module was discovered by plugins
        updateModuleWithBuildConfig(branch, modulesByFolder, malformedFiles,
            discoveredBuildConfig, pluginDiscoveredModulesInFolder.iterator().next());
      } else { // more than one plugin-discovered modules
        // it is an error to have more than one plugin-discovered modules and a build config in the same folder
        // because we won't know how to combine the build config with the different plugin-discovered modules
        modulesByFolder.removeAll(folder);
        String message = String.format("Build configuration %s coexists with more that one plugin-discovered modules (%s). " +
            "Only a single discovered module can exist within a folder that contains a build configuration file, " +
            "otherwise it is not known how to apply the build configuration to each of the discovered modules",
            discoveredBuildConfig.getPath(), pluginDiscoveredModulesInFolder.stream().map(Module::getName).collect(Collectors.joining(" ,")));
        malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-build-config-combination-with-multiple-discovered-modules",
            discoveredBuildConfig.getPath(), message));
      }
    }

    // We have resolved the build configurations for all folders that contain build configs
    // We also need to resolve build configurations for the modules that were discovered by plugins in folders without
    // explicit build configs. In that case the build config will be determined by the buildpack that is specified in
    // the discovered module.
    Set<String> foldersWithBuildConfigs = buildConfigDiscoveryResult.getDiscoveredBuildConfigs().stream().map(DiscoveredBuildConfig::getFolder).collect(Collectors.toSet());
    resolveBuildConfigurationsForModulesWithoutABuildConfig(branch, modulesByFolder, foldersWithBuildConfigs, malformedFiles);

  }

  private void resolveBuildConfigurationsForModulesWithoutABuildConfig(GitInfo branch,
                                                                       Multimap<String, Module> modulesByFolder,
                                                                       Set<String> foldersWithBuildConfigs,
                                                                       Set<MalformedFile> malformedFiles) {
    for (Entry<String, Module> entry : modulesByFolder.entries()) {
      if (foldersWithBuildConfigs.contains(entry.getKey())) {
        continue;
      }

      Module module = entry.getValue();
      if (!module.getBuildpack().isPresent()) {
        String message = String.format("The discovered module %s (type: %s) in path %s doesn't specify any buildpack " +
            "and no other build configuration was found in the folder. Please update the relevant discovery plugin " +
            "to specify a buildpack", module.getName(), module.getType(), module.getPath());
        malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-or-nonexistent-buildpack",
            module.getPath(), message));
        modulesByFolder.remove(entry.getKey(), entry.getValue());
        continue;
      }

      try {
        BuildConfig buildpackBuildConfig = buildConfigUtils.getConfigForBuildpackOnBranch(module.getBuildpack().get());
        BuildConfig resolvedBuildConfig = buildConfigUtils.addMissingBuildConfigSettings(buildpackBuildConfig);
        // remove and re-add the entry adding the original and the resolved build config.
        modulesByFolder.remove(entry.getKey(), entry.getValue());

        Module updatedModule = createUpdatedModule(module, buildpackBuildConfig, resolvedBuildConfig);
        modulesByFolder.put(entry.getKey(), updatedModule);
      } catch (Exception e) {
        malformedFiles.add(new MalformedFile(branch.getId().get(), "invalid-or-nonexistent-buildpack", module.getPath(), e.getMessage()));
        modulesByFolder.remove(entry.getKey(), entry.getValue());
      }
    }
  }

  private void updateModuleWithBuildConfig(GitInfo branch,
                                           Multimap<String, Module> modulesByPath,
                                           Set<MalformedFile> malformedFiles,
                                           DiscoveredBuildConfig discoveredBuildConfig,
                                           Module pluginDiscoveredModule) {

    BuildConfig buildConfig = discoveredBuildConfig.getBuildConfig();
    String buildConfigFolder = discoveredBuildConfig.getFolder();

    BuildConfig resolvedBuildConfig;
    if (buildConfig.getBuildpack().isPresent()) { // We will ignore the buildpack specified in the discovered module
      Optional<BuildConfig> resolvedBuildConfigOptional = getResolvedBuildConfig(buildConfig, malformedFiles, branch);

      if (!resolvedBuildConfigOptional.isPresent()) {
        return;
      }
      resolvedBuildConfig = resolvedBuildConfigOptional.get();
    } else if (pluginDiscoveredModule.getBuildpack().isPresent()){
      Optional<BuildConfig> resolvedBuildConfigOptional =
          getResolvedBuildConfigFromPrimaryBuildConfigAndBuildPack(buildConfig, pluginDiscoveredModule.getBuildpack().get(), malformedFiles, branch);
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

    // If during this build the plugin has newly discovered or rediscovered the module we want to merge the
    // plugin discovered dependencies unless the build config specifies ignorePluginDiscoveredDependencies = true
    Set<Dependency> buildConfigDependencies = buildConfig.getDepends();
    Set<Dependency> buildConfigProvidedDependencies = buildConfig.getProvides();
    Set<Dependency> pluginDiscoveredDependencies = Collections.emptySet();
    Set<Dependency> pluginDiscoveredProvidedDependencies = Collections.emptySet();
    if (!resolvedBuildConfig.isIgnorePluginDiscoveredDependencies() && pluginDiscoveredModule.getClass() == DiscoveredModule.class) {
      pluginDiscoveredDependencies = ((DiscoveredModule)pluginDiscoveredModule).getDependencyInfo().getPluginDiscoveredDependencies();
      pluginDiscoveredProvidedDependencies = ((DiscoveredModule)pluginDiscoveredModule).getDependencyInfo().getPluginDiscoveredProvidedDependencies();
    }

    // replace the module with a DiscoveredModule that contains the resolved build config and dependencies
    // i.e. if we have a combination of build config and plugin discovered module we always create a DiscoveredModule
    // even if the plugin module has not been rediscovered in this build, because we need to add the possibly changed dependencies that
    // may exist in the resolved build config.
    modulesByPath.remove(buildConfigFolder, pluginDiscoveredModule);

    modulesByPath.put(buildConfigFolder,
        new DiscoveredModule(
            pluginDiscoveredModule.getId(),
            pluginDiscoveredModule.getName(),
            pluginDiscoveredModule.getType(),
            pluginDiscoveredModule.getPath(),
            pluginDiscoveredModule.getGlob(),
            pluginDiscoveredModule.isActive(),
            pluginDiscoveredModule.getCreatedTimestamp(),
            pluginDiscoveredModule.getUpdatedTimestamp(),
            pluginDiscoveredModule.getBuildpack(),
            new DependencyInfo(buildConfigDependencies, buildConfigProvidedDependencies, pluginDiscoveredDependencies, pluginDiscoveredProvidedDependencies),
            Optional.of(buildConfig),
            Optional.of(resolvedBuildConfig)));
  }

  private void createModuleFromBuildConfig(GitInfo branch,
                                   Multimap<String, Module> modulesByFolder,
                                   Collection<Module> modulesInFolder,
                                   Set<MalformedFile> malformedFiles,
                                   DiscoveredBuildConfig discoveredBuildConfig) {

    String buildConfigFolder = discoveredBuildConfig.getFolder();

    // if a module created out of the build config exists already we will delete it so it can be replaced by the
    // resolved config
    if (modulesInFolder.size() == 1) {
      modulesByFolder.remove(buildConfigFolder, modulesInFolder.iterator().next());
    } else if (modulesInFolder.size() > 1) {
      modulesByFolder.removeAll(buildConfigFolder);
      String message = String.format("More than one build configuration based modules found in folder %s. Only " +
          "one module based on a build configuration file can exist in a folder. This is probably a bug in " +
          "Blazar code.", buildConfigFolder);
      malformedFiles.add(new MalformedFile(branch.getId().get(), "multiple-build-config-modules-in-folder",
          discoveredBuildConfig.getPath(), message));
    }

    BuildConfig buildConfig = discoveredBuildConfig.getBuildConfig();

    Optional<BuildConfig> resolvedBuildConfigOptional = getResolvedBuildConfig(buildConfig, malformedFiles, branch);

    if (!resolvedBuildConfigOptional.isPresent()) {
      return;
    }

    BuildConfig resolvedBuildConfig = resolvedBuildConfigOptional.get();

    if (!checkCanBuild(discoveredBuildConfig, resolvedBuildConfig, branch, malformedFiles)) {
      return;
    }

    String moduleName = moduleName(branch, discoveredBuildConfig.getFolder());
    modulesByFolder.put(buildConfigFolder,
        new DiscoveredModule(
            moduleName,
            MODULE_TYPE_OF_BUILD_CONFIG,
            discoveredBuildConfig.getPath(),
            discoveredBuildConfig.getGlob(),
            buildConfig.getBuildpack(),
            new DependencyInfo(buildConfig.getDepends(), buildConfig.getProvides(), Collections.emptySet(), Collections.emptySet()),
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

  private Module createUpdatedModule(Module module, BuildConfig buildConfig, BuildConfig resolvedBuildConfig) {
    Module updatedModule;
    if (module.getClass() == DiscoveredModule.class) {
      DiscoveredModule updatedDiscoveredModule = new DiscoveredModule(
          module.getName(),
          module.getType(),
          module.getPath(),
          module.getGlob(),
          module.getBuildpack(),
          ((DiscoveredModule)module).getDependencyInfo(),
          Optional.of(buildConfig),
          Optional.of(resolvedBuildConfig));

      updatedModule = updatedDiscoveredModule;
    } else {
      updatedModule = new Module(
          module.getId(),
          module.getName(),
          module.getType(),
          module.getPath(),
          module.getGlob(),
          module.isActive(),
          module.getCreatedTimestamp(),
          module.getUpdatedTimestamp(),
          module.getBuildpack(),
          Optional.of(buildConfig),
          Optional.of(resolvedBuildConfig));
    }
    return  updatedModule;
  }
}
