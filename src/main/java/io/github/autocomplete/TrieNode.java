package io.github.autocomplete;

import java.util.HashMap;
import java.util.Map;

/**
 * Узел префиксного дерева
 */
class TrieNode {
  private final Map<Character, TrieNode> children;
  private int frequency;

  public TrieNode() {
    this.children = new HashMap<>();
    this.frequency = 0;
  }

  public Map<Character, TrieNode> getChildren() {
    return children;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public void incrementFrequency() {
    this.frequency++;
  }
}
