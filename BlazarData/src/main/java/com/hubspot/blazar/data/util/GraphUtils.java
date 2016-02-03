package com.hubspot.blazar.data.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import java.util.Set;

public enum GraphUtils {
  INSTANCE;

  public <V> SetMultimap<V, V> findAllPaths(SetMultimap<V, V> edges) {
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

  public <V> SetMultimap<V, V> transitiveReduction(SetMultimap<V, V> paths) {
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

  private <V> Set<V> vertices(SetMultimap<V, V> graph) {
    return ImmutableSet.<V>builder().addAll(graph.keySet()).addAll(graph.values()).build();
  }
}
