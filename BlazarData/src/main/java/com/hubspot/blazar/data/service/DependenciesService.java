package com.hubspot.blazar.data.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class DependenciesService {
  private final DependenciesDao dependenciesDao;

  @Inject
  public DependenciesService(DependenciesDao dependenciesDao) {
    this.dependenciesDao = dependenciesDao;
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
