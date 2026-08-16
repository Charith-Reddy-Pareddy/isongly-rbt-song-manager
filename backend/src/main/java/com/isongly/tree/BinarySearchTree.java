package com.isongly.tree;

/**
 * A generic Binary Search Tree that sorts elements by their natural ordering.
 *
 * @param <T> the type of elements stored in this tree
 */
public class BinarySearchTree<T extends Comparable<T>> implements SortedCollection<T> {

  protected BSTNode<T> root;

  @Override
  public void insert(T data) throws NullPointerException {
    if (data == null) {
      throw new NullPointerException("Null values cannot be inserted into the tree.");
    }
    BSTNode<T> newNode = new BSTNode<>(data);
    if (root == null) {
      root = newNode;
    } else {
      insertHelper(newNode, root);
    }
  }

  /**
   * Recursively inserts newNode into the subtree rooted at subtree, following
   * standard BST placement (duplicates go to the left).
   */
  protected void insertHelper(BSTNode<T> newNode, BSTNode<T> subtree) {
    if (subtree == null) {
      return;
    }
    int compareResult = newNode.data.compareTo(subtree.data);
    if (compareResult <= 0) {
      if (subtree.getLeft() == null) {
        subtree.setLeft(newNode);
        newNode.setUp(subtree);
      } else {
        insertHelper(newNode, subtree.getLeft());
      }
    } else {
      if (subtree.getRight() == null) {
        subtree.setRight(newNode);
        newNode.setUp(subtree);
      } else {
        insertHelper(newNode, subtree.getRight());
      }
    }
  }

  @Override
  public boolean contains(Comparable<T> data) {
    return findHelper(data, root) != null;
  }

  private BSTNode<T> findHelper(Comparable<T> data, BSTNode<T> subtree) {
    if (subtree == null) {
      return null;
    }
    int compareResult = data.compareTo(subtree.getData());
    if (compareResult < 0) {
      return findHelper(data, subtree.getLeft());
    } else if (compareResult > 0) {
      return findHelper(data, subtree.getRight());
    } else {
      return subtree;
    }
  }

  @Override
  public int size() {
    return countNodes(root);
  }

  private int countNodes(BSTNode<T> currentNode) {
    if (currentNode == null) {
      return 0;
    }
    return 1 + countNodes(currentNode.getLeft()) + countNodes(currentNode.getRight());
  }

  @Override
  public boolean isEmpty() {
    return root == null;
  }

  @Override
  public void clear() {
    root = null;
  }
}
