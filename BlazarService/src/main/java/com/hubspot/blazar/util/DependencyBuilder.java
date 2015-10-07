package com.hubspot.blazar.util;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.DependenciesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@Singleton
public class DependencyBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(DependencyBuilder.class);

  private final BuildService buildService;
  private final DependenciesService dependenciesService;
  private final BuildDefinitionService buildDefinitionService;

  @Inject
  public DependencyBuilder(BuildService buildService,
                           DependenciesService dependenciesService,
                           BuildDefinitionService buildDefinitionService,
                           EventBus eventBus) {
    this.buildService = buildService;
    this.dependenciesService = dependenciesService;
    this.buildDefinitionService = buildDefinitionService;

    eventBus.register(this);
  }

  @Subscribe
  public void triggerDownstreamBuilds(Build build) {
    if (build.getState() == State.SUCCEEDED && shouldTriggerDownstreamBuilds(build)) {
      int moduleId = build.getModuleId();
      BuildDefinition definition = buildDefinitionService.getByModule(moduleId).get();
      DependencyGraph graph = dependenciesService.buildDependencyGraph(definition.getGitInfo());
      LOG.info("Computed dependency graph {}", graph);
      Set<Integer> downstreamModules = graph.getDownstreamModules(moduleId);
      LOG.info("Found downstream modules {} for module {}", downstreamModules, moduleId);
      Set<Integer> modulesToBuild = graph.removeRedundantModules(downstreamModules);
      LOG.info("Going to trigger builds for modules {}", modulesToBuild);
      for (int module : modulesToBuild) {
        buildService.enqueue(buildDefinitionService.getByModule(module).get());
      }
    }
  }

  private boolean shouldTriggerDownstreamBuilds(Build build) {
    Optional<BuildConfig> buildConfig = build.getBuildConfig();
    if (buildConfig.isPresent() && manualBuildSpecified(buildConfig.get())) {
      return true;
    } else {
      String whitelist = Objects.firstNonNull(System.getenv("BUILD_WHITELIST"), "");
      List<String> whitelistedRepos= Splitter.on(',').trimResults().omitEmptyStrings().splitToList(whitelist);
      BuildDefinition definition = buildDefinitionService.getByModule(build.getModuleId()).get();

      return whitelistedRepos.contains(definition.getGitInfo().getRepository());
    }
  }

  private static boolean manualBuildSpecified(BuildConfig buildConfig) {
    return !buildConfig.getCmds().isEmpty() || buildConfig.getBuildpack().isPresent();
  }
}
