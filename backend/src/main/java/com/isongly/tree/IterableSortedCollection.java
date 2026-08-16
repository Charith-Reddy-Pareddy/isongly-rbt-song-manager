package com.isongly.tree;

/**
 * A SortedCollection that is also iterable in ascending order, optionally
 * bounded by a minimum and/or maximum value.
 */
public interface IterableSortedCollection<T extends Comparable<T>>
    extends SortedCollection<T>, Iterable<T> {

  /** Sets the inclusive lower bound used by iterators created after this call, or null to clear it. */
  void setIteratorMin(Comparable<T> min);

  /** Sets the inclusive upper bound used by iterators created after this call, or null to clear it. */
  void setIteratorMax(Comparable<T> max);
}
