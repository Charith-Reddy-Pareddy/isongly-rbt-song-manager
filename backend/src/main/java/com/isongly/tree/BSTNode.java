package com.isongly.tree;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * A node for a Binary Search Tree: holds a data value and references to its
 * parent and two children.
 */
public class BSTNode<T> {

  protected T data;
  protected BSTNode<T> up = null;
  protected BSTNode<T> left = null;
  protected BSTNode<T> right = null;

  public BSTNode(T data) { this.data = data; }

  public T getData() { return this.data; }
  public BSTNode<T> getLeft() { return this.left; }
  public BSTNode<T> getRight() { return this.right; }
  public BSTNode<T> getUp() { return this.up; }

  public void setData(T newData) { this.data = newData; }
  public void setUp(BSTNode<T> newParent) { this.up = newParent; }
  public void setLeft(BSTNode<T> newLeftChild) { this.left = newLeftChild; }
  public void setRight(BSTNode<T> newRightChild) { this.right = newRightChild; }

  /**
   * @return true when this node has a parent and is the right child of that parent
   */
  public boolean isRightChild() {
    return this.getUp() != null && this.getUp().getRight() == this;
  }

  @Override
  public String toString() {
    return this.data.toString();
  }

  /** Level-order traversal of the subtree rooted at this node. */
  public String toLevelOrderString() {
    Queue<BSTNode<T>> nodeList = new LinkedList<>();
    nodeList.add(this);
    StringBuilder sb = new StringBuilder();
    sb.append("[ ");
    while (!nodeList.isEmpty()) {
      if (nodeList.peek().getLeft() != null) {
        nodeList.add(nodeList.peek().getLeft());
      }
      if (nodeList.peek().getRight() != null) {
        nodeList.add(nodeList.peek().getRight());
      }
      sb.append(nodeList.poll().toString());
      sb.append(nodeList.isEmpty() ? " ]" : ", ");
    }
    return sb.toString();
  }

  /** In-order traversal of the subtree rooted at this node. */
  public String toInOrderString() {
    Stack<BSTNode<T>> stack = new Stack<>();
    stack.push(this);
    while (stack.peek().getLeft() != null) {
      stack.push(stack.peek().getLeft());
    }
    StringBuilder sb = new StringBuilder();
    sb.append("[ ");
    while (!stack.isEmpty()) {
      BSTNode<T> current = stack.pop();
      sb.append(current.toString());
      if (current.getRight() != null) {
        stack.push(current.getRight());
        while (stack.peek().getLeft() != null) {
          stack.push(stack.peek().getLeft());
        }
      }
      sb.append(stack.isEmpty() ? " ]" : ", ");
    }
    return sb.toString();
  }
}
