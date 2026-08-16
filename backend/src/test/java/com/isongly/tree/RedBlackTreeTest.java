package com.isongly.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedBlackTreeTest {

  @Test
  void emptyTreeHasNullRoot() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    assertNull(tree.root);
  }

  @Test
  void nullInsertThrows() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    Exception exception = assertThrows(NullPointerException.class, () -> tree.insert(null));
    assertEquals("Null values cannot be inserted into the tree.", exception.getMessage());
  }

  @Test
  void redAuntScenarioRecolors() {
    RedBlackTree<String> tree = new RedBlackTree<>();
    tree.insert("D");
    tree.insert("B");
    tree.insert("F");
    tree.insert("A"); // triggers red-aunt recoloring
    tree.insert("C");
    tree.insert("E");
    tree.insert("G");

    RBTNode<String> root = (RBTNode<String>) tree.root;

    assertEquals("[ D(b), B(b), F(b), A(r), C(r), E(r), G(r) ]", root.toLevelOrderString());
    assertEquals("[ A(r), B(b), C(r), D(b), E(r), F(b), G(r) ]", root.toInOrderString());
    assertFalse(root.isRed());
    assertFalse(root.getLeft().isRed());
    assertFalse(root.getRight().isRed());
  }

  @Test
  void blackAuntScenarioRotatesAndRecolors() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    int[] values = {7, 14, 18, 23, 1, 11, 20, 29, 25, 27};
    for (int v : values) {
      tree.insert(v);
    }

    RBTNode<Integer> root = (RBTNode<Integer>) tree.root;

    assertEquals("[ 20(b), 14(r), 25(r), 7(b), 18(b), 23(b), 29(b), 1(r), 11(r), 27(r) ]",
        root.toLevelOrderString());
    assertEquals("[ 1(r), 7(b), 11(r), 14(r), 18(b), 20(b), 23(b), 25(r), 27(r), 29(b) ]",
        root.toInOrderString());
    assertFalse(root.isRed());
  }

  @Test
  void blackAuntWithNullAuntHandledCorrectly() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    tree.insert(50);
    tree.insert(30);
    tree.insert(70);
    tree.insert(20);
    tree.insert(40); // parent has no sibling (null aunt)

    RBTNode<Integer> root = (RBTNode<Integer>) tree.root;
    assertEquals("[ 50(b), 30(b), 70(b), 20(r), 40(r) ]", root.toLevelOrderString());
    assertEquals("[ 20(r), 30(b), 40(r), 50(b), 70(b) ]", root.toInOrderString());
    assertFalse(root.isRed());
  }

  @Test
  void zigzagCaseRebalancesWithTwoRotations() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    tree.insert(4);
    tree.insert(2);
    tree.insert(3); // zigzag: 3 is right child of left child

    RBTNode<Integer> root = (RBTNode<Integer>) tree.root;
    assertEquals("[ 3(b), 2(r), 4(r) ]", root.toLevelOrderString());
    assertEquals("[ 2(r), 3(b), 4(r) ]", root.toInOrderString());
  }

  @Test
  void multiLevelZigzagCase() {
    RedBlackTree<String> tree = new RedBlackTree<>();
    String[] values = {"M", "H", "R", "F", "J", "P", "V", "I", "K", "L"};
    for (String v : values) {
      tree.insert(v);
    }

    RBTNode<String> root = (RBTNode<String>) tree.root;
    assertEquals("[ J(b), H(r), M(r), F(b), I(b), K(b), R(b), L(r), P(r), V(r) ]",
        root.toLevelOrderString());
    assertEquals("[ F(b), H(r), I(b), J(b), K(b), L(r), M(r), P(r), R(b), V(r) ]",
        root.toInOrderString());
  }

  @Test
  void duplicateEntriesAreKeptAndPlacedToTheLeft() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    tree.insert(10);
    tree.insert(20);
    tree.insert(10);
    tree.insert(10);

    RBTNode<Integer> root = (RBTNode<Integer>) tree.root;
    assertEquals("[ 10(b), 10(b), 20(b), 10(r) ]", root.toLevelOrderString());
  }

  @Test
  void rootAlwaysEndsUpBlack() {
    RedBlackTree<Integer> tree = new RedBlackTree<>();
    tree.insert(1000);
    tree.insert(2000);
    tree.insert(3000);

    assertFalse(((RBTNode<Integer>) tree.root).isRed());
    assertEquals("[ 1000(r), 2000(b), 3000(r) ]", tree.root.toInOrderString());
  }
}
