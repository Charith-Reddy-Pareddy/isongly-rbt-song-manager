package com.isongly.tree;

/**
 * A self-balancing binary search tree that maintains red-black properties
 * through rotations and recoloring on insertion.
 */
public class RedBlackTree<T extends Comparable<T>> extends BSTRotation<T> {

  /**
   * Inserts a new element while maintaining red-black properties.
   *
   * @param data the element to insert
   * @throws NullPointerException if data is null
   */
  @Override
  public void insert(T data) throws NullPointerException {
    if (data == null) {
      throw new NullPointerException("Null values cannot be inserted into the tree.");
    }
    RBTNode<T> newNode = new RBTNode<>(data);
    newNode.isRed = true;

    if (this.root == null) {
      this.root = newNode;
    } else {
      super.insertHelper(newNode, this.root);
      ensureRedProperty(newNode);
    }

    if (((RBTNode<T>) this.root).isRed) {
      ((RBTNode<T>) this.root).flipColor();
    }
  }

  /**
   * Repairs a red-red violation caused by newRedNode having a red parent, and
   * any further violations that repair introduces further up the tree.
   *
   * @param newRedNode a newly inserted red node, or a node turned red by a previous repair
   */
  protected void ensureRedProperty(RBTNode<T> newRedNode) {
    if (newRedNode == this.root || newRedNode.getUp() == null
        || !newRedNode.isRed || !newRedNode.getUp().isRed) {
      return;
    }

    RBTNode<T> parent = newRedNode.getUp();
    RBTNode<T> grandparent = parent.getUp();
    if (grandparent == null) {
      return;
    }
    RBTNode<T> aunt = (parent == grandparent.getLeft()) ? grandparent.getRight() : grandparent.getLeft();

    if (aunt != null && aunt.isRed) {
      parent.flipColor();
      aunt.flipColor();
      grandparent.flipColor();
      ensureRedProperty(grandparent);
    } else {
      handlePotentialViolations(newRedNode, parent, grandparent);
    }
  }

  private void handlePotentialViolations(RBTNode<T> newRedNode, RBTNode<T> parent, RBTNode<T> grandparent) {
    boolean newIsLeftChild = (newRedNode == parent.getLeft());
    boolean parentIsLeftChild = (parent == grandparent.getLeft());

    if (newIsLeftChild == parentIsLeftChild) {
      handleStraightLine(parent, grandparent);
    } else {
      handleZigzag(newRedNode, parent, grandparent);
    }
  }

  /**
   * Handles the case where the new node and its parent are aligned in the
   * same direction relative to the grandparent: a single rotation suffices.
   */
  private void handleStraightLine(RBTNode<T> parent, RBTNode<T> grandparent) {
    super.rotate(parent, grandparent);
    parent.flipColor();
    grandparent.flipColor();
  }

  /**
   * Handles the case where the new node and its parent are not aligned:
   * two rotations are needed to resolve the violation.
   */
  private void handleZigzag(RBTNode<T> newRedNode, RBTNode<T> parent, RBTNode<T> grandparent) {
    super.rotate(newRedNode, parent);
    super.rotate(newRedNode, grandparent);
    newRedNode.flipColor();
    grandparent.flipColor();
  }
}
