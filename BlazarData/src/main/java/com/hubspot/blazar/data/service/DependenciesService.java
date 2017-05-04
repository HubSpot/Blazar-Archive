package com.hubspot.blazar.data.service;

import static com.hubspot.blazar.base.ModuleDependency.Source.BUILD_CONFIG;
import static com.hubspot.blazar.base.ModuleDependency.Source.PLUGIN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.hubspot.blazar.base.Dependency;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.graph.Edge;
import com.hubspot.blazar.data.dao.DependenciesDao;
import com.hubspot.blazar.data.util.GraphUtils;

@Singleton
public class DependenciesService {
  private final DependenciesDao dependenciesDao;
  private static final Logger LOG = LoggerFactory.getLogger(DependenciesService.class);

  @Inject
  public DependenciesService(DependenciesDao dependenciesDao) {
    this.dependenciesDao = dependenciesDao;
  }

  public DependencyGraph buildInterProjectDependencyGraph(Set<Module> modulesTriggered) {
    long start = System.currentTimeMillis();
    SetMultimap<Integer, Integer> graph = computeGraphFromRootModules(-1, modulesTriggered);

    long startGraph = System.currentTimeMillis();
    Set<Integer> s = new HashSet<>();
    for (Map.Entry<Integer, Integer> i : graph.entries()) {
      s.add(i.getKey());
      s.add(i.getValue());
    }
    LOG.info("graph is of size {}", s.size());
    int maxWidth = 0;
    for (Map.Entry<Integer, Collection<Integer>> entry  : graph.asMap().entrySet()) {
      if ( maxWidth < entry.getValue().size()) {
        maxWidth = entry.getValue().size();
      }
    }

    LOG.info("Graph is {} at its widest", maxWidth);

    SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(graph);
    LOG.info("Transitive reduction calculation took {}", System.currentTimeMillis() - startGraph);
    List<Integer> topologicalSort = GraphUtils.INSTANCE.topologicalSort(graph);
    Map<Integer, Set<Integer>> transitiveReductionMap = new HashMap<>(Multimaps.asMap(transitiveReduction));
    DependencyGraph dependencyGraph = new DependencyGraph(transitiveReductionMap, topologicalSort);
    LOG.info("Building graph took {}", System.currentTimeMillis() - start);
    return dependencyGraph;
  }

  public DependencyGraph buildDependencyGraph(GitInfo gitInfo, Set<Module> allModules) {
    // one module can't have any deps
    if (allModules.size() == 1) {
      int moduleId = allModules.iterator().next().getId().get();
      List<Integer> topologicalSort = Collections.singletonList(moduleId);
      Map<Integer, Set<Integer>> transitiveReduction = Collections.singletonMap(moduleId, Collections.<Integer>emptySet());

      return new DependencyGraph(transitiveReduction, topologicalSort);
    } else {
      SetMultimap<Integer, Integer> fullGraph = computeGraphFromRootModules(gitInfo.getId().get(), allModules);

      Set<Integer> moduleIds = new HashSet<>();
      for (Module module : allModules) {
        moduleIds.add(module.getId().get());
      }

      SetMultimap<Integer, Integer> repoGraph = GraphUtils.INSTANCE.retain(fullGraph, moduleIds);
      SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(repoGraph);
      List<Integer> topologicalSort = GraphUtils.INSTANCE.topologicalSort(transitiveReduction);
      List<Integer> missingModules = findMissingModules(topologicalSort, allModules);
      Map<Integer, Set<Integer>> transitiveReductionMap = new HashMap<>(Multimaps.asMap(transitiveReduction));
      for (int missingModule : missingModules) {
        transitiveReductionMap.put(missingModule, Collections.<Integer>emptySet());
      }

      return new DependencyGraph(transitiveReductionMap, concat(missingModules, topologicalSort));
    }
  }

  private SetMultimap<Integer, Integer> computeGraphFromRootModules(int branchId, Set<Module> rootModules) {
    SetMultimap<Integer, Integer> graph = HashMultimap.create();

    Set<Integer> seenModules = new HashSet<>();
    Queue<Integer> moduleQueue = new LinkedList<>();
    List<Long> queryTimes = new ArrayList<>();

    for (Module module : rootModules) {
      moduleQueue.add(module.getId().get());
    }

    while (true) {
      Set<Integer> modulesToProcess = new HashSet<>(moduleQueue);
      modulesToProcess.removeAll(seenModules);
      moduleQueue.clear();

      if (modulesToProcess.isEmpty()) {
        break;
      }

      long queryStart = System.currentTimeMillis();
      Set<Edge> edges = dependenciesDao.getEdges(branchId, modulesToProcess);
      long queryEnd = System.currentTimeMillis();
      LOG.info("Query for {} took {}", modulesToProcess, queryEnd - queryStart);
      queryTimes.add(queryEnd - queryStart);

      for (Edge edge : edges) {
        graph.put(edge.getSource(), edge.getTarget());
        moduleQueue.add(edge.getTarget());
      }

      seenModules.addAll(modulesToProcess);
    }

    long sum = 0;
    long max = 0;
    long min = Long.MAX_VALUE;
    for (long i : queryTimes) {
      sum += i;
      if (i > max) {
        max = i;
      }
      if (i < min) {
        min = i;
      }
    }
    long average = queryTimes.size() == 0 ? 0 : sum / queryTimes.size();
    LOG.info("MysqlQueries max: {} min: {} ct: {} each: {} total: {}", max, min, queryTimes.size(), average, sum);
    return graph;
  }

  @Transactional
  public void insert(DiscoveredModule module) {
    dependenciesDao.insertProvidedDependencies(module.getBuildConfigProvidedDependencies());
    dependenciesDao.insertProvidedDependencies(module.getPluginDiscoveredProvidedDependencies());

    dependenciesDao.insertDependencies(module.getBuildConfigDependencies());
    dependenciesDao.insertDependencies(module.getPluginDiscoveredDependencies());
  }

  @Transactional
  public void update(DiscoveredModule module) {
    updateProvidedDependencies(module);
    updateDependencies(module);
  }

  @Transactional
  public void delete(int moduleId) {
    dependenciesDao.deleteProvidedDependencies(moduleId);
    dependenciesDao.deleteDependencies(moduleId);
  }

  public Set<Dependency> getProvided(int moduleId) {
    return dependenciesDao.getProvided(moduleId);
  }

  public Set<Dependency> getDependencies(int moduleId) {
    return dependenciesDao.getDependencies(moduleId);
  }

  public Set<GitInfo> getBranchesWithNonVersionedDependencies() {
    return dependenciesDao.getBranchesWithNonVersionedDependencies();
  }

  private void updateProvidedDependencies(DiscoveredModule module) {
    if (!module.getBuildConfigProvidedDependencies().isEmpty()) {
      dependenciesDao.deleteProvidedDependenciesBySource(module.getId().get(), BUILD_CONFIG);
      dependenciesDao.insertProvidedDependencies(module.getBuildConfigProvidedDependencies());
    }

    if (!module.getPluginDiscoveredProvidedDependencies().isEmpty()) {
      dependenciesDao.deleteProvidedDependenciesBySource(module.getId().get(), PLUGIN);
      dependenciesDao.insertProvidedDependencies(module.getPluginDiscoveredProvidedDependencies());
    }
  }

  private void updateDependencies(DiscoveredModule module) {
    if (!module.getBuildConfigDependencies().isEmpty()) {
      dependenciesDao.deleteDependenciesBySource(module.getId().get(), BUILD_CONFIG);
      dependenciesDao.insertDependencies(module.getBuildConfigDependencies());
    }

    if (!module.getPluginDiscoveredProvidedDependencies().isEmpty()) {
      dependenciesDao.deleteDependenciesBySource(module.getId().get(), PLUGIN);
      dependenciesDao.insertDependencies(module.getPluginDiscoveredDependencies());
    }
  }

  private static List<Integer> findMissingModules(List<Integer> topologicalSort, Set<Module> allModules) {
    List<Integer> missing = new ArrayList<>();
    for (Module module : allModules) {
      if (!topologicalSort.contains(module.getId().get())) {
        missing.add(module.getId().get());
      }
    }

    Collections.sort(missing);
    return missing;
  }

  private static List<Integer> concat(List<Integer> list1, List<Integer> list2) {
    return ImmutableList.copyOf(Iterables.concat(list1, list2));
  }
}
