package com.hubspot.blazar.data.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public enum GraphUtils {
  INSTANCE;

  public <V> List<V> topologicalSort(SetMultimap<V, V> transitiveReduction) {
    SetMultimap<V, V> graph = HashMultimap.create(transitiveReduction);

    List<V> sorted = new ArrayList<>();
    Set<V> roots = new TreeSet<>();
    for (V vertex : graph.keySet()) {
      if (incomingVertices(graph, vertex).isEmpty()) {
        roots.add(vertex);
      }
    }

    while (!roots.isEmpty()) {
      Iterator<V> iterator = roots.iterator();
      V root = iterator.next();
      sorted.add(root);
      iterator.remove();

      Set<V> children = graph.removeAll(root);
      for (V child : children) {
        if (incomingVertices(graph, child).isEmpty()) {
          roots.add(child);
        }
      }
    }

    return sorted;
  }

  public <V> SetMultimap<V, V> transitiveReduction(SetMultimap<V, V> edges) {
    SetMultimap<V, V> paths = findAllPaths(edges);

    SetMultimap<V, V> reduced = HashMultimap.create(paths);
    Set<V> vertices = vertices(paths);

    for (V vertexI : vertices) {
      for (V vertexJ : vertices) {
        if (reduced.get(vertexI).contains(vertexJ)) {
          for (V vertexK : vertices) {
            if (reduced.get(vertexJ).contains(vertexK)) {
              reduced.get(vertexI).remove(vertexK);
            }
          }
        }
      }
    }

    return reduced;
  }

  private <V> Set<V> incomingVertices(SetMultimap<V, V> graph, V target) {
    Set<V> incomingVertices = new HashSet<>();
    for (Entry<V, V> path : graph.entries()) {
      if (path.getValue() == target) {
        incomingVertices.add(path.getKey());
      }
    }

    return incomingVertices;
  }

  private <V> SetMultimap<V, V> findAllPaths(SetMultimap<V, V> edges) {
    SetMultimap<V, V> paths = HashMultimap.create(edges);
    Set<V> vertices = vertices(paths);

    for (V vertexI : vertices) {
      for (V vertexJ : vertices) {
        if (paths.get(vertexJ).contains(vertexI)) {
          for (V vertexK : vertices) {
            if (paths.get(vertexI).contains(vertexK)) {
              paths.get(vertexJ).add(vertexK);
            }
          }
        }
      }
    }

    return paths;
  }

  private <V> Set<V> vertices(SetMultimap<V, V> graph) {
    return ImmutableSet.<V>builder().addAll(graph.keySet()).addAll(graph.values()).build();
  }
}
