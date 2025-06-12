package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TrieNodeTest {

  @Test
  void constructor_InitializesEmptyNode() {
    TrieNode node = new TrieNode();

    assertTrue(node.getChildren().isEmpty());
    assertEquals(0, node.getFrequency());
  }

  @Test
  void frequencyMethods_WorkCorrectly() {
    TrieNode node = new TrieNode();

    node.setFrequency(5);
    assertEquals(5, node.getFrequency());

    node.incrementFrequency();
    assertEquals(6, node.getFrequency());

    node.setFrequency(0);
    assertEquals(0, node.getFrequency());
  }

  @Test
  void childrenMap_IsMutable() {
    TrieNode node = new TrieNode();
    TrieNode child = new TrieNode();

    node.getChildren().put('a', child);
    assertSame(child, node.getChildren().get('a'));
  }
}
