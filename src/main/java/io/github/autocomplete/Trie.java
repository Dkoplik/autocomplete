package io.github.autocomplete;

import io.github.autocomplete.util.Candidate;
import io.github.autocomplete.util.FixedSizeMinHeap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация префиксного дерева
 */
class Trie {
  private final TrieNode root;

  public Trie() {
    this.root = new TrieNode();
  }

  /**
   * Вставляет слово в дерево, увеличивая его частоту на 1
   */
  public void insert(String word) {
    TrieNode current = root;
    for (char c : word.toCharArray()) {
      Map<Character, TrieNode> children = current.getChildren();
      TrieNode child = children.get(c);
      if (child == null) {
        child = new TrieNode();
        children.put(c, child);
      }
      current = child;
    }
    current.incrementFrequency();
  }

  /**
   * Возвращает частоту слова (сколько раз оно было добавлено)
   */
  public int getFrequency(String word) {
    TrieNode node = getNode(word);
    return node != null ? node.getFrequency() : 0;
  }

  /**
   * Удаляет слово из дерева (устанавливает частоту в 0)
   */
  public void remove(String word) {
    TrieNode node = getNode(word);
    if (node != null) {
      node.setFrequency(0);
    }
  }

  /**
   * Возвращает узел, соответствующий данному слову
   */
  private TrieNode getNode(String word) {
    TrieNode current = root;
    for (char c : word.toCharArray()) {
      Map<Character, TrieNode> children = current.getChildren();
      current = children.get(c);
      if (current == null) {
        return null;
      }
    }
    return current;
  }

  /**
   * Возвращает все слова с их частотами
   */
  public Map<String, Integer> getAllWords() {
    Map<String, Integer> words = new HashMap<>();
    collectWords(root, new StringBuilder(), words);
    return words;
  }

  private void collectWords(TrieNode node, StringBuilder currentWord, Map<String, Integer> words) {
    if (node.getFrequency() > 0) {
      words.put(currentWord.toString(), node.getFrequency());
    }

    for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
      currentWord.append(entry.getKey());
      collectWords(entry.getValue(), currentWord, words);
      currentWord.deleteCharAt(currentWord.length() - 1);
    }
  }

  /**
   * Ищет автодополнения для префикса
   * 
   * @param prefix Префикс для поиска
   * @param limit Максимальное количество результатов
   * @return Список кандидатов, отсортированных по частоте
   */
  public List<Candidate> findCompletions(String prefix, int limit) {
    TrieNode node = getNode(prefix);
    if (node == null) {
      return Collections.emptyList();
    }

    FixedSizeMinHeap heap = new FixedSizeMinHeap(limit);
    StringBuilder current = new StringBuilder(prefix);
    collectCompletions(node, current, heap);
    return heap.toSortedList();
  }

  private void collectCompletions(TrieNode node, StringBuilder current, FixedSizeMinHeap heap) {
    if (node.getFrequency() > 0) {
      heap.add(new Candidate(current.toString(), node.getFrequency()));
    }

    for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
      current.append(entry.getKey());
      collectCompletions(entry.getValue(), current, heap);
      current.deleteCharAt(current.length() - 1);
    }
  }

  /**
   * Возвращает топ-N самых частых слов
   * 
   * @param n Количество возвращаемых слов
   * @return Список кандидатов, отсортированных по частоте (от наиболее к наименее частому)
   */
  public List<Candidate> getTopFrequentWords(int n) {
    if (n <= 0) {
      return Collections.emptyList();
    }

    FixedSizeMinHeap heap = new FixedSizeMinHeap(n);
    collectTopWords(root, new StringBuilder(), heap);
    return heap.toSortedList();
  }

  private void collectTopWords(TrieNode node, StringBuilder currentWord, FixedSizeMinHeap heap) {
    if (node.getFrequency() > 0) {
      heap.add(new Candidate(currentWord.toString(), node.getFrequency()));
    }

    for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
      currentWord.append(entry.getKey());
      collectTopWords(entry.getValue(), currentWord, heap);
      currentWord.deleteCharAt(currentWord.length() - 1);
    }
  }
}
