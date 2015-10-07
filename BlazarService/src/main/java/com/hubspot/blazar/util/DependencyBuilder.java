package com.hubspot.blazar.util;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.DependenciesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  public void handleBuildStateChange(Build build) throws IOException {
    if (build.getState() == State.SUCCEEDED && shouldTriggerDownstreamBuilds(build)) {
      int moduleId = build.getModuleId();
      BuildDefinition definition = buildDefinitionService.getByModule(moduleId).get();
      Multimap<Integer, Integer> graph = buildGraph(definition.getGitInfo());

      Collection<Integer> downstreamModules = graph.get(moduleId);
      if (downstreamModules == null) {
        LOG.info("No downstream modules found for module {}", moduleId);
      } else {
        LOG.info("Found downstream modules {} for modules {}", downstreamModules, moduleId);
        Set<Integer> modulesToBuild = new HashSet<>(downstreamModules);
        for (int module : downstreamModules) {
          if (graph.containsKey(module)) {
            modulesToBuild.removeAll(graph.get(module));
          }
        }

        LOG.info("Going to trigger builds for modules {}", modulesToBuild);
        for (int module : modulesToBuild) {
          buildService.enqueue(buildDefinitionService.getByModule(module).get());
        }
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

  private Multimap<Integer, Integer> buildGraph(GitInfo gitInfo) {
    Map<String, Integer> providerMap = asMap(dependenciesService.getProvides(gitInfo));

    Multimap<Integer, Integer> graph = HashMultimap.create();
    for (ModuleDependency dependency : dependenciesService.getDepends(gitInfo)) {
      if (providerMap.containsKey(dependency.getName())) {
        graph.put(providerMap.get(dependency.getName()), dependency.getModuleId());
      }
    }

    return graph;
  }

  private Map<String, Integer> asMap(Set<ModuleDependency> dependencies) {
    Map<String, Integer> dependencyMap = new HashMap<>();
    for (ModuleDependency dependency : dependencies) {
      dependencyMap.put(dependency.getName(), dependency.getModuleId());
    }

    return dependencyMap;
  }
}
