package com.hubspot.blazar.data.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.blazar.data.dao.DependenciesDao;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DependenciesService {
  private final DependenciesDao dependenciesDao;

  @Inject
  public DependenciesService(DependenciesDao dependenciesDao) {
    this.dependenciesDao = dependenciesDao;
  }

  public DependencyGraph buildDependencyGraph(GitInfo gitInfo) {
    Map<String, Integer> providerMap = asMap(getProvides(gitInfo));

    Multimap<Integer, Integer> graph = HashMultimap.create();
    for (ModuleDependency dependency : getDepends(gitInfo)) {
      if (providerMap.containsKey(dependency.getName())) {
        graph.put(providerMap.get(dependency.getName()), dependency.getModuleId());
      }
    }

    return new DependencyGraph(graph);
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

  private static Map<String, Integer> asMap(Set<ModuleDependency> dependencies) {
    Map<String, Integer> dependencyMap = new HashMap<>();
    for (ModuleDependency dependency : dependencies) {
      dependencyMap.put(dependency.getName(), dependency.getModuleId());
    }

    return dependencyMap;
  }
}
