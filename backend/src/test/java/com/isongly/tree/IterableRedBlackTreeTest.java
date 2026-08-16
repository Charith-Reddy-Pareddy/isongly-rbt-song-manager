package com.isongly.tree;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IterableRedBlackTreeTest {

  private <T extends Comparable<T>> List<T> collect(IterableRedBlackTree<T> tree) {
    List<T> results = new ArrayList<>();
    for (T value : tree) {
      results.add(value);
    }
    return results;
  }

  @Test
  void iteratesInAscendingOrderWithNoBounds() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    for (int v : new int[]{50, 30, 70, 20, 40, 60, 80}) {
      tree.insert(v);
    }
    assertEquals(Arrays.asList(20, 30, 40, 50, 60, 70, 80), collect(tree));
  }

  @Test
  void respectsMinAndMaxBoundsTogether() {
    IterableRedBlackTree<String> tree = new IterableRedBlackTree<>();
    tree.setIteratorMin("banana");
    tree.setIteratorMax("fig");
    for (String v : new String[]{"apple", "banana", "cherry", "date", "fig", "egg", "mango"}) {
      tree.insert(v);
    }
    assertEquals(Arrays.asList("banana", "cherry", "date", "egg", "fig"), collect(tree));
  }

  @Test
  void respectsOnlyMinBound() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    tree.setIteratorMin(40);
    for (int v : new int[]{50, 30, 70, 20, 40, 60, 80}) {
      tree.insert(v);
    }
    assertEquals(Arrays.asList(40, 50, 60, 70, 80), collect(tree));
  }

  @Test
  void respectsOnlyMaxBound() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    tree.setIteratorMax(40);
    for (int v : new int[]{10, 20, 30, 40, 50}) {
      tree.insert(v);
    }
    assertEquals(Arrays.asList(10, 20, 30, 40), collect(tree));
  }

  @Test
  void reversedBoundsYieldNoResults() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    tree.setIteratorMin(70);
    tree.setIteratorMax(30);
    for (int v : new int[]{20, 30, 40, 50, 60, 70, 80}) {
      tree.insert(v);
    }
    assertTrue(collect(tree).isEmpty());
  }

  @Test
  void duplicatesAreIncludedAndOrderedCorrectly() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    for (int v : new int[]{50, 30, 50, 20, 30, 70, 30}) {
      tree.insert(v);
    }
    assertEquals(Arrays.asList(20, 30, 30, 30, 50, 50, 70), collect(tree));
  }

  @Test
  void duplicatesRespectBounds() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    tree.setIteratorMin(25);
    tree.setIteratorMax(55);
    for (int v : new int[]{50, 30, 50, 20, 30, 70, 30}) {
      tree.insert(v);
    }
    assertEquals(Arrays.asList(30, 30, 30, 50, 50), collect(tree));
  }

  @Test
  void boundsCanBeNarrowedBetweenIterations() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    for (int v : new int[]{5, 15, 25, 35, 45, 55, 65, 75, 85}) {
      tree.insert(v);
    }

    tree.setIteratorMin(20);
    tree.setIteratorMax(80);
    assertEquals(Arrays.asList(25, 35, 45, 55, 65, 75), collect(tree));

    tree.setIteratorMin(30);
    tree.setIteratorMax(70);
    assertEquals(Arrays.asList(35, 45, 55, 65), collect(tree));
  }

  @Test
  void singleElementTreeHandlesExactBounds() {
    IterableRedBlackTree<Integer> tree = new IterableRedBlackTree<>();
    tree.insert(50);
    assertEquals(List.of(50), collect(tree));

    tree.setIteratorMin(50);
    tree.setIteratorMax(50);
    assertEquals(List.of(50), collect(tree));
  }
}
