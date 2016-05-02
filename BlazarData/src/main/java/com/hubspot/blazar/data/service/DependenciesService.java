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
import com.hubspot.blazar.base.graph.Edge;
import com.hubspot.blazar.data.dao.DependenciesDao;
import com.hubspot.blazar.data.util.GraphUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    SetMultimap<Integer, Integer> graph = HashMultimap.create();

    Set<Integer> seenModules = new HashSet<>();
    Queue<Integer> moduleQueue = new LinkedList<>();
    List<Long> queryTimes = new ArrayList<>();

    for (Module module : modulesTriggered) {
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
      Set<Edge> edges = dependenciesDao.getEdges(modulesToProcess);
      long queryEnd = System.currentTimeMillis();
      queryTimes.add(queryEnd - queryStart);

      for (Edge edge : edges) {
        graph.put(edge.getSource(), edge.getTarget());
        moduleQueue.add(edge.getTarget());
      }

      seenModules.addAll(modulesToProcess);
    }

    long startGraph = System.currentTimeMillis();
    SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(graph);
    LOG.info("Transitive reduction calculation took {}", System.currentTimeMillis()-startGraph);
    long startSort = System.currentTimeMillis();
    List<Integer> topologicalSort = GraphUtils.INSTANCE.topologicalSort(graph);
    LOG.info("Topological sort calculation took {}", System.currentTimeMillis()-startSort);
    Map<Integer, Set<Integer>> transitiveReductionMap = new HashMap<>(Multimaps.asMap(transitiveReduction));
    DependencyGraph dependencyGraph = new DependencyGraph(transitiveReductionMap, topologicalSort);
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
    return dependencyGraph;
  }

  public DependencyGraph buildDependencyGraph(GitInfo gitInfo, Set<Module> allModules) {
    Set<Edge> edges = dependenciesDao.getEdges(gitInfo);

    SetMultimap<Integer, Integer> graph = HashMultimap.create();
    for (Edge edge : edges) {
      graph.put(edge.getSource(), edge.getTarget());
    }

    SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(graph);
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
}
