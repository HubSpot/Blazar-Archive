package com.hubspot.blazar.data.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.Module;

public class GraphUtilsTest {
  private static final SetMultimap<Integer, Integer> EDGES = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(1, 2)
      .put(1, 3)
      .put(3, 4)
      .put(3, 5)
      .put(4, 5)
      .put(5, 2)
      .build();

  private static final SetMultimap<Integer, Integer> REDUCED = ImmutableSetMultimap.<Integer, Integer>builder()
      //.put(1, 2) Not in transitive reduction, handled by 1 -> 3 -> 4 -> 5 -> 2
      .put(1, 3)
      .put(3, 4)
      //.put(3, 5) Not in transitive reduction, handled by 3 -> 4 -> 5
      .put(4, 5)
      .put(5, 2)
      .build();

  private static final SetMultimap<Integer, Integer> RETAINED = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(1, 5)
      .build();

  private static final SetMultimap<Integer, Integer> BLAZAR = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(278, 280)
      .put(280, 277)
      .put(280, 281)
      .put(280, 279)
      .put(279, 276)
      .build();

  @Test
  public void testTransitiveReduction() {
    assertThat(GraphUtils.INSTANCE.transitiveReduction(EDGES).asMap()).isEqualTo(REDUCED.asMap());
  }

  @Test
  public void itSortsModulesAccordingToTopologicalSort() {
    SetMultimap<Integer, Integer> transitiveReduction = GraphUtils.INSTANCE.transitiveReduction(EDGES);
    List<Integer> topologicalSort = GraphUtils.INSTANCE.topologicalSort(transitiveReduction);
    Map<Integer, Set<Integer>> tree = new HashMap<>();
    EDGES.entries().stream().forEach(entry -> tree.put(entry.getKey(), new HashSet<>(entry.getValue())));
    DependencyGraph graph = new DependencyGraph(tree, topologicalSort);

    Set<Module> modules = ImmutableSet.<Module>builder()
        .add(makeModuleWithId(1))
        .add(makeModuleWithId(2))
        .add(makeModuleWithId(3))
        .add(makeModuleWithId(4))
        .add(makeModuleWithId(5))
        .build();

    List<Module> expectedOrder = ImmutableList.<Module>builder()
        .add(makeModuleWithId(1))
        .add(makeModuleWithId(3))
        .add(makeModuleWithId(4))
        .add(makeModuleWithId(5))
        .add(makeModuleWithId(2))
        .build();

    assertThat(graph.orderByTopologicalSort(modules)).isEqualTo(expectedOrder);
  }

  @Test
  public void testTopologicalSort() {
    assertThat(GraphUtils.INSTANCE.topologicalSort(REDUCED)).isEqualTo(Arrays.asList(1, 3, 4, 5, 2));
  }

  @Test
  public void testBlazarTopologicalSort() {
    assertThat(GraphUtils.INSTANCE.topologicalSort(BLAZAR)).isEqualTo(Arrays.asList(278, 280, 277, 279, 281, 276));
  }

  @Test
  public void testRetain() {
    assertThat(GraphUtils.INSTANCE.retain(EDGES, ImmutableSet.of(1, 5)).asMap()).isEqualTo(RETAINED.asMap());
  }

  private static Module makeModuleWithId(int id) {
    return new Module(Optional.of(id), "test-module", "config", "/", "/*", true, System.currentTimeMillis(), System.currentTimeMillis(), Optional.absent());
  }
}
