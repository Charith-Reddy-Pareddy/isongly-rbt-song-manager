package com.isongly.tree;

/**
 * An ADT for data structures that support storing a collection of comparable
 * values in their natural ordering.
 */
public interface SortedCollection<T extends Comparable<T>> {

  /**
   * Inserts a new data value into the sorted collection.
   *
   * @param data the new value being inserted
   * @throws NullPointerException if data argument is null
   */
  void insert(T data) throws NullPointerException;

  /**
   * Checks whether data is stored in the collection.
   *
   * @param data the value to check for
   * @return true if the collection contains data one or more times
   */
  boolean contains(Comparable<T> data);

  /**
   * Counts the number of values in the collection, including duplicates.
   *
   * @return the number of values in the collection
   */
  int size();

  /**
   * @return true if the collection contains 0 values, false otherwise
   */
  boolean isEmpty();

  /**
   * Removes all values from the collection.
   */
  void clear();
}
