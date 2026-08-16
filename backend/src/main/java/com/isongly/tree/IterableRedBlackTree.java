package com.isongly.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * A RedBlackTree that supports iterating over its values in sorted, ascending
 * order, optionally bounded by a minimum and/or maximum value.
 */
public class IterableRedBlackTree<T extends Comparable<T>> extends RedBlackTree<T>
    implements IterableSortedCollection<T> {

  private T iteratorMin = null;
  private T iteratorMax = null;

  @SuppressWarnings("unchecked")
  public void setIteratorMin(Comparable<T> min) {
    this.iteratorMin = (T) min;
  }

  @SuppressWarnings("unchecked")
  public void setIteratorMax(Comparable<T> max) {
    this.iteratorMax = (T) max;
  }

  /**
   * Returns an iterator bounded by the values most recently passed to
   * setIteratorMin/setIteratorMax (or unbounded on either end if those were
   * never called or were called with null).
   */
  public Iterator<T> iterator() {
    return new RBTIterator<>(root, iteratorMin, iteratorMax);
  }

  /** In-order iterator over a subtree, respecting optional min/max bounds. */
  protected static class RBTIterator<R> implements Iterator<R> {

    private final Comparable<R> min;
    private final Comparable<R> max;
    private final Stack<BSTNode<R>> stack;

    public RBTIterator(BSTNode<R> root, Comparable<R> min, Comparable<R> max) {
      this.min = min;
      this.max = max;
      this.stack = new Stack<>();
      buildStackHelper(root);
    }

    /**
     * Pushes onto the stack every ancestor, along the path from node down to
     * the smallest in-bounds value in this subtree, that could still be
     * visited in-order.
     */
    private void buildStackHelper(BSTNode<R> node) {
      if (node == null) {
        return;
      }
      if (min != null && min.compareTo(node.getData()) > 0) {
        buildStackHelper(node.getRight());
      } else if (max != null && max.compareTo(node.getData()) < 0) {
        buildStackHelper(node.getLeft());
      } else {
        stack.push(node);
        buildStackHelper(node.getLeft());
      }
    }

    public boolean hasNext() {
      return !stack.isEmpty();
    }

    public R next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more elements in the tree to iterate over.");
      }
      BSTNode<R> currentNode = stack.pop();
      if (currentNode.getRight() != null) {
        buildStackHelper(currentNode.getRight());
      }
      return currentNode.getData();
    }
  }
}
