package com.isongly.tree;

/**
 * Extends BinarySearchTree with the rotation operation needed by
 * self-balancing trees such as RedBlackTree.
 */
public class BSTRotation<T extends Comparable<T>> extends BinarySearchTree<T> {

  /**
   * Rotates the provided child node into the parent's position. When child is
   * a left child of parent, this performs a right rotation; when child is a
   * right child, this performs a left rotation.
   *
   * @param child  the node being rotated from child to parent position
   * @param parent the node being rotated from parent to child position
   * @throws NullPointerException     when either argument is null
   * @throws IllegalArgumentException when child is not an immediate child of parent
   */
  protected void rotate(BSTNode<T> child, BSTNode<T> parent)
      throws NullPointerException, IllegalArgumentException {
    if (child == null || parent == null) {
      throw new NullPointerException("Child or parent node cannot be null.");
    }
    if (parent.getLeft() != child && parent.getRight() != child) {
      throw new IllegalArgumentException("The provided child and parent nodes are not related.");
    }

    if (parent.getLeft() == child) {
      rotateRight(child, parent);
    } else {
      rotateLeft(child, parent);
    }
  }

  private void rotateLeft(BSTNode<T> child, BSTNode<T> parent) {
    parent.setRight(child.getLeft());
    if (child.getLeft() != null) {
      child.getLeft().setUp(parent);
    }

    child.setLeft(parent);
    child.setUp(parent.getUp());

    if (parent.getUp() == null) {
      this.root = child;
    } else if (parent.getUp().getRight() == parent) {
      parent.getUp().setRight(child);
    } else {
      parent.getUp().setLeft(child);
    }

    parent.setUp(child);
  }

  private void rotateRight(BSTNode<T> child, BSTNode<T> parent) {
    parent.setLeft(child.getRight());
    if (child.getRight() != null) {
      child.getRight().setUp(parent);
    }

    child.setRight(parent);
    child.setUp(parent.getUp());

    if (parent.getUp() == null) {
      this.root = child;
    } else if (parent.getUp().getLeft() == parent) {
      parent.getUp().setLeft(child);
    } else {
      parent.getUp().setRight(child);
    }

    parent.setUp(child);
  }
}
