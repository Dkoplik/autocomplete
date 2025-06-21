package io.github.autocomplete;

import java.util.HashMap;
import java.util.Map;

/**
 * Узел префиксного дерева.
 */
class TrieNode {
  private final Map<Character, TrieNode> children;
  private int frequency;

  /**
   * Создаёт новый узел.
   */
  public TrieNode() {
    this.children = new HashMap<>();
    this.frequency = 0;
  }

  /**
   * Возвращает дочерние узлы.
   * 
   * @return Дочерние узлы
   */
  public Map<Character, TrieNode> getChildren() {
    return children;
  }

  /**
   * Возвращает частоту узла.
   * 
   * @return Частота узла
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * Устанавливает частоту узла.
   * 
   * @param frequency Частота узла
   * 
   * @throws IllegalArgumentException Если frequency меньше 0
   */
  public void setFrequency(int frequency) {
    if (frequency < 0) {
      throw new IllegalArgumentException("frequency cannot be negative");
    }
    this.frequency = frequency;
  }

  /**
   * Увеличивает частоту узла на 1.
   */
  public void incrementFrequency() {
    this.frequency++;
  }
}
