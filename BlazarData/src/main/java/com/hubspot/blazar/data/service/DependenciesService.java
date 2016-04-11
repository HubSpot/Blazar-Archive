package com.hubspot.blazar.data.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleDependency;
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
    SetMultimap<Integer, Integer> edges = HashMultimap.create();

    // Turn triggered modules into ModuleDependencies
    Set<ModuleDependency> moduleDependenciesTriggered = new HashSet<>();
    for (Module m : modulesTriggered ) {
      LOG.info("InterProjectBuild has been triggered for module {}({})", m.getName(), m.getId().get());
      moduleDependenciesTriggered.addAll(getProvides(ImmutableSet.of(m.getId().get())));
    }

    Stack<ModuleDependency> stack = new Stack<>();
    stack.addAll(moduleDependenciesTriggered);
    Set<ModuleDependency> seen = new HashSet<>();
    List<Long> queryTimes = new ArrayList<>();
    while (!stack.empty()) {
      ModuleDependency m = stack.pop();
      seen.add(m);
      LOG.debug("Found Node {}", m);
      while(stack.remove(m)) {
        LOG.debug("Removed copy of {} from stack", m);
      }
      long s = System.currentTimeMillis();
      Set<ModuleDependency> newItems = getProvidesFromModuleDepends(m);
      queryTimes.add(System.currentTimeMillis() - s);
      for (ModuleDependency newItem : newItems) {
        LOG.debug("Added {} <- {}", m, newItem);
        edges.put(m.getModuleId(), newItem.getModuleId());
      }
      newItems.removeAll(seen);
      stack.addAll(newItems);
    }
    long startGraph = System.currentTimeMillis();
    SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(edges);
    LOG.info("Transitive reduction calculation took {}", System.currentTimeMillis()-startGraph);
    long startSort = System.currentTimeMillis();
    List<Integer> topologicalSort = GraphUtils.INSTANCE.topologicalSort(edges);
    LOG.info("Topological sort calculation took {}", System.currentTimeMillis()-startSort);
    Map<Integer, Set<Integer>> transitiveReductionMap = new HashMap<>(Multimaps.asMap(transitiveReduction));
    DependencyGraph graph = new DependencyGraph(transitiveReductionMap, topologicalSort);
    long sum = 0;
    long max = 0;
    long min = Long.MAX_VALUE;
    for (long i : queryTimes) {
      sum = sum + i;
      if (max < i) {
        max = i;
      }
      if (min > i) {
        min = i;
      }
    }
    long average = queryTimes.size() == 0 ? sum : sum/queryTimes.size();
    LOG.info("MysqlQueries max: {} min: {} ct: {} each: {} total: {}", max, min, queryTimes.size(), average, sum);
    LOG.info("Building graph took {}", System.currentTimeMillis() - start);
    return graph;
  }

  public DependencyGraph buildDependencyGraph(GitInfo gitInfo, Set<Module> allModules) {
    Map<String, Integer> providerMap = asMap(getProvides(gitInfo));

    SetMultimap<Integer, Integer> edges = HashMultimap.create();
    for (ModuleDependency dependency : getDepends(gitInfo)) {
      if (providerMap.containsKey(dependency.getName())) {
        edges.put(providerMap.get(dependency.getName()), dependency.getModuleId());
      }
    }

    SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(edges);
    List<Integer> topologicalSort = GraphUtils.INSTANCE.topologicalSort(transitiveReduction);
    List<Integer> missingModules = findMissingModules(topologicalSort, allModules);
    Map<Integer, Set<Integer>> transitiveReductionMap = new HashMap<>(Multimaps.asMap(transitiveReduction));
    for (int missingModule : missingModules) {
      transitiveReductionMap.put(missingModule, Collections.<Integer>emptySet());
    }

    return new DependencyGraph(transitiveReductionMap, concat(missingModules, topologicalSort));
  }

  @Transactional
  public void insert(DiscoveredModule module) {
    dependenciesDao.insertProvides(module.getProvides());
    dependenciesDao.insertDepends(module.getDepends());
  }

  @Transactional
  public void update(DiscoveredModule module) {
    updateProvides(module);
    updateDepends(module);
  }

  @Transactional
  public void delete(int moduleId) {
    dependenciesDao.deleteProvides(moduleId);
    dependenciesDao.deleteDepends(moduleId);
  }

  private Set<ModuleDependency> getProvides(Set<Integer> moduleIds) {
    if (moduleIds.size() > 0) {
      return dependenciesDao.getProvides(moduleIds);
    }
    return new HashSet<>();
  }

  private Set<ModuleDependency> getProvidesFromModuleDepends(ModuleDependency moduleDependency) {
    return dependenciesDao.getProvidesFromModuleDepends(moduleDependency);
  }

  private Set<ModuleDependency> getProvides(GitInfo gitInfo) {
    return dependenciesDao.getProvides(gitInfo);
  }

  private Set<ModuleDependency> getDepends(GitInfo gitInfo) {
    return dependenciesDao.getDepends(gitInfo);
  }

  private void updateProvides(DiscoveredModule module) {
    dependenciesDao.deleteProvides(module.getId().get());
    dependenciesDao.insertProvides(module.getProvides());
  }

  private void updateDepends(DiscoveredModule module) {
    dependenciesDao.deleteDepends(module.getId().get());
    dependenciesDao.insertDepends(module.getDepends());
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

  private static Map<String, Integer> asMap(Set<ModuleDependency> dependencies) {
    Map<String, Integer> dependencyMap = new HashMap<>();
    for (ModuleDependency dependency : dependencies) {
      dependencyMap.put(dependency.getName(), dependency.getModuleId());
    }

    return dependencyMap;
  }
}
