package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TrieNodeTest {

  @Test
  void constructorInitializesEmptyNode() {
    TrieNode node = new TrieNode();

    assertTrue(node.getChildren().isEmpty());
    assertEquals(0, node.getFrequency());
  }

  @Test
  void frequencyMethodsWorkCorrectly() {
    TrieNode node = new TrieNode();

    node.setFrequency(5);
    assertEquals(5, node.getFrequency());

    node.incrementFrequency();
    assertEquals(6, node.getFrequency());

    node.setFrequency(0);
    assertEquals(0, node.getFrequency());
  }

  @Test
  void childrenMapIsMutable() {
    TrieNode node = new TrieNode();
    TrieNode child = new TrieNode();

    node.getChildren().put('a', child);
    assertSame(child, node.getChildren().get('a'));
  }
}
