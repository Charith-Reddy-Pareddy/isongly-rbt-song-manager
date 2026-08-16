package com.isongly.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BSTRotationTest {

  /**
   * Builds a small, deterministically-shaped tree:
   * <pre>
   *         5
   *       /   \
   *      3     8
   *     / \   / \
   *    2   4 7   9
   * </pre>
   */
  private BSTRotation<Integer> sampleTree() {
    BSTRotation<Integer> tree = new BSTRotation<>();
    for (int v : new int[]{5, 3, 8, 2, 4, 7, 9}) {
      tree.insert(v);
    }
    return tree;
  }

  @Test
  void leftRotationOnNonRootPairPreservesOrderAndRelinksNodes() {
    BSTRotation<Integer> tree = sampleTree();
    String expectedInOrder = tree.root.toInOrderString();

    BSTNode<Integer> parent = tree.root.getLeft(); // 3
    BSTNode<Integer> child = parent.getRight();    // 4, right child of 3 -> left rotation

    tree.rotate(child, parent);

    assertEquals(expectedInOrder, tree.root.toInOrderString(), "rotation must preserve sorted order");
    assertSame(child, tree.root.getLeft(), "4 should take 3's place under the root");
    assertSame(parent, child.getLeft(), "3 should become 4's left child");
    assertSame(tree.root, child.getUp());
    assertSame(child, parent.getUp());
    assertNull(parent.getRight(), "4's old left child (null) becomes 3's right child");
  }

  @Test
  void rightRotationOnNonRootPairPreservesOrderAndRelinksNodes() {
    BSTRotation<Integer> tree = sampleTree();
    String expectedInOrder = tree.root.toInOrderString();

    BSTNode<Integer> parent = tree.root.getLeft(); // 3
    BSTNode<Integer> child = parent.getLeft();     // 2, left child of 3 -> right rotation

    tree.rotate(child, parent);

    assertEquals(expectedInOrder, tree.root.toInOrderString());
    assertSame(child, tree.root.getLeft(), "2 should take 3's place under the root");
    assertSame(parent, child.getRight(), "3 should become 2's right child");
    assertSame(tree.root, child.getUp());
    assertSame(child, parent.getUp());
    assertNull(parent.getLeft(), "2's old right child (null) becomes 3's left child");
  }

  @Test
  void rotationInvolvingRootUpdatesTreeRoot() {
    BSTRotation<Integer> tree = sampleTree();
    String expectedInOrder = tree.root.toInOrderString();
    BSTNode<Integer> oldRoot = tree.root;
    BSTNode<Integer> newRoot = oldRoot.getLeft(); // 3

    tree.rotate(newRoot, oldRoot);

    assertSame(newRoot, tree.root, "3 should become the new root");
    assertNull(tree.root.getUp(), "new root must have no parent");
    assertSame(oldRoot, newRoot.getRight(), "5 should become 3's right child");
    assertEquals(expectedInOrder, tree.root.toInOrderString());
  }

  @Test
  void sequentialRotationsRemainAValidBst() {
    BSTRotation<Integer> tree = sampleTree();
    String expectedInOrder = tree.root.toInOrderString();

    tree.rotate(tree.root.getLeft(), tree.root); // 3 becomes root
    tree.rotate(tree.root.getRight(), tree.root); // 5 rotates back up on the right side

    assertEquals(expectedInOrder, tree.root.toInOrderString(),
        "in-order sequence must be unaffected by any number of rotations");
  }

  @Test
  void rotateWithNullArgumentsThrowsNullPointerException() {
    BSTRotation<Integer> tree = sampleTree();
    assertThrows(NullPointerException.class, () -> tree.rotate(null, null));
    assertThrows(NullPointerException.class, () -> tree.rotate(tree.root, null));
    assertThrows(NullPointerException.class, () -> tree.rotate(null, tree.root));
  }

  @Test
  void rotateWithUnrelatedNodesThrowsIllegalArgumentException() {
    BSTRotation<Integer> tree = sampleTree();
    // root.getLeft().getLeft() (node 2) is a grandchild of root, not a direct child
    BSTNode<Integer> notAChild = tree.root.getLeft().getLeft();
    assertThrows(IllegalArgumentException.class, () -> tree.rotate(notAChild, tree.root));
  }
}
