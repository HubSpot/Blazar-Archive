package com.hubspot.blazar.data.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public enum GraphUtils {
  INSTANCE;

  public <V extends Comparable<V>> List<V> topologicalSort(SetMultimap<V, V> transitiveReduction) {
    SetMultimap<V, V> graph = TreeMultimap.create(transitiveReduction);

    List<V> sorted = new ArrayList<>();
    Deque<V> roots = new ArrayDeque<>();
    for (V vertex : graph.keySet()) {
      if (incomingVertices(graph, vertex).isEmpty()) {
        roots.add(vertex);
      }
    }

    while (!roots.isEmpty()) {
      V root = roots.removeFirst();
      sorted.add(root);

      Set<V> children = graph.removeAll(root);
      for (V child : children) {
        if (incomingVertices(graph, child).isEmpty()) {
          roots.addLast(child);
        }
      }
    }

    return sorted;
  }

  // working from the bottom of the graph should be a lot faster
  public <V> SetMultimap<V, V> retain(SetMultimap<V, V> graph, Set<V> vertices) {
    SetMultimap<V, V> reduced = HashMultimap.create();
    SetMultimap<V, V> inverted = HashMultimap.create();
    Deque<Edge<V>> queue = new ArrayDeque<>();
    Set<Edge<V>> processed = new HashSet<>();

    for (Entry<V, V> entry : graph.entries()) {
      inverted.put(entry.getValue(), entry.getKey());
      if (vertices.contains(entry.getValue())) {
        queue.add(new Edge<>(entry.getKey(), entry.getValue()));
      }
    }

    while (!queue.isEmpty()) {
      Edge<V> edge = queue.removeFirst();
      processed.add(edge);
      if (vertices.contains(edge.getSource())) {
        reduced.put(edge.getSource(), edge.getTarget());
      } else {
        for (V parent : inverted.get(edge.getSource())) {
          Edge<V> newEdge = new Edge<>(parent, edge.getTarget());
          if (!processed.contains(newEdge)) {
            queue.addLast(newEdge);
          }
        }
      }
    }

    return reduced;
  }

  public <V> SetMultimap<V, V> transitiveReduction(SetMultimap<V, V> edges) {
    SetMultimap<V, V> paths = findAllPaths(edges);

    SetMultimap<V, V> reduced = HashMultimap.create(paths);
    Set<V> vertices = vertices(paths);

    for (V vertexJ : vertices) {
      for (V vertexI : vertices) {
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
      if (path.getValue().equals(target)) {
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

  private static class Edge<T> {
    private final T source;
    private final T target;

    public Edge(T source, T target) {
      this.source = source;
      this.target = target;
    }

    public T getSource() {
      return source;
    }

    public T getTarget() {
      return target;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Edge edge = (Edge) o;
      return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
    }

    @Override
    public int hashCode() {
      return Objects.hash(source, target);
    }
  }
}
