package com.isongly.tree;

/** A node in a RedBlackTree, adding a red/black color flag to BSTNode. */
public class RBTNode<T> extends BSTNode<T> {

  protected boolean isRed = true;

  public RBTNode(T data) { super(data); }

  @Override
  public RBTNode<T> getLeft() {
    return (RBTNode<T>) this.left;
  }

  @Override
  public RBTNode<T> getRight() {
    return (RBTNode<T>) this.right;
  }

  @Override
  public RBTNode<T> getUp() {
    return (RBTNode<T>) this.up;
  }

  public boolean isRed() {
    return this.isRed;
  }

  /** Flips this node's color from red to black or black to red. */
  public void flipColor() {
    this.isRed = !this.isRed;
  }

  @Override
  public String toString() {
    return this.data.toString() + (this.isRed() ? "(r)" : "(b)");
  }
}
