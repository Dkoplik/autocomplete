package io.github.autocomplete;

import io.github.autocomplete.util.FixedSizeMinHeap;
import io.github.autocomplete.util.WordFrequency;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Реализация префиксного дерева с подсчётом частоты слов.
 */
class Trie {
  private final TrieNode root;

  /**
   * Создаёт пустое дерево.
   */
  public Trie() {
    this.root = new TrieNode();
  }

  /**
   * Вставляет слово в дерево, увеличивая его частоту на 1.
   *
   * @param word Слово для вставки
   *
   * @throws IllegalArgumentException Если word равен null или пустой строке
   */
  public void insert(String word) {
    if (word == null || word.isEmpty()) {
      throw new IllegalArgumentException("word cannot be null or empty");
    }

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
   * Возвращает частоту слова (сколько раз оно было добавлено).
   *
   * @param word Слово для поиска
   *
   * @return Частота слова
   *
   * @throws IllegalArgumentException Если word равен null или пустой строке
   */
  public int getFrequency(String word) {
    if (word == null || word.isEmpty()) {
      throw new IllegalArgumentException("word cannot be null or empty");
    }

    TrieNode node = getNode(word);
    return node != null ? node.getFrequency() : 0;
  }

  /**
   * Удаляет слово из дерева (устанавливает частоту в 0 или полностью удаляет, если trulyDelete).
   *
   * @param word Слово для удаления
   *
   * @param trulyDelete Если true, полностью удаляет слово и неиспользуемые узлы
   *
   * @throws IllegalArgumentException Если word равен null или пустой строке
   */
  public void remove(String word, boolean trulyDelete) {
    if (word == null || word.isEmpty()) {
      throw new IllegalArgumentException("word cannot be null or empty");
    }

    if (!trulyDelete) {
      TrieNode node = getNode(word);
      if (node != null) {
        node.setFrequency(0);
      }
      return;
    }
    removeAndPrune(root, word, 0);
  }

  /**
   * Удаляет слово из дерева (устанавливает частоту в 0).
   *
   * @param word Слово для удаления
   *
   * @throws IllegalArgumentException Если word равен null или пустой строке
   */
  public void remove(String word) {
    remove(word, false);
  }

  /**
   * Рекурсивно удаляет слово и "обрезает" неиспользуемые узлы.
   *
   * @return true, если текущий узел можно удалить из родителя
   */
  private boolean removeAndPrune(TrieNode node, String word, int index) {
    if (index == word.length()) {
      if (node.getFrequency() > 0) {
        node.setFrequency(0);
      }
      return node.getChildren().isEmpty();
    }
    char c = word.charAt(index);
    TrieNode child = node.getChildren().get(c);
    if (child == null) {
      return false;
    }
    boolean shouldDeleteChild = removeAndPrune(child, word, index + 1);
    if (shouldDeleteChild) {
      node.getChildren().remove(c);
    }
    return node.getChildren().isEmpty() && node.getFrequency() == 0;
  }

  /**
   * Возвращает узел, соответствующий данному слову.
   *
   * @param word Слово для поиска
   *
   * @return Узел, соответствующий данному слову
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
   * Возвращает все слова с их частотами.
   *
   * @return Все слова с их частотами
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
   * Ищет автодополнения для префикса (без учёта опечаток).
   *
   * @param prefix Префикс для поиска
   *
   * @param limit Максимальное количество результатов
   *
   * @return Список кандидатов, отсортированных по частоте
   *
   * @throws IllegalArgumentException Если prefix равен null или пустой строке ИЛИ limit меньше 1
   */
  public List<WordFrequency> findCompletions(String prefix, int limit) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("prefix cannot be null or empty");
    }
    if (limit < 1) {
      throw new IllegalArgumentException("limit cannot be less than 1");
    }

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
      heap.add(new WordFrequency(current.toString(), node.getFrequency()));
    }

    for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
      current.append(entry.getKey());
      collectCompletions(entry.getValue(), current, heap);
      current.deleteCharAt(current.length() - 1);
    }
  }

  /**
   * Возвращает топ-N самых частых слов.
   *
   * @param n Количество слов
   *
   * @return Список из топ-N самых частых слов
   *
   * @throws IllegalArgumentException Если n меньше или равно 0
   */
  public List<WordFrequency> getTopFrequentWords(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n cannot be less than or equal to 0");
    }

    FixedSizeMinHeap heap = new FixedSizeMinHeap(n);
    collectTopWords(root, new StringBuilder(), heap);
    return heap.toSortedList();
  }

  private void collectTopWords(TrieNode node, StringBuilder currentWord, FixedSizeMinHeap heap) {
    if (node.getFrequency() > 0) {
      heap.add(new WordFrequency(currentWord.toString(), node.getFrequency()));
    }

    for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
      currentWord.append(entry.getKey());
      collectTopWords(entry.getValue(), currentWord, heap);
      currentWord.deleteCharAt(currentWord.length() - 1);
    }
  }

  /**
   * Очищает всё дерево, удаляя все слова и частоты.
   */
  public void clear() {
    root.getChildren().clear();
    root.setFrequency(0);
  }

  /**
   * Возвращает список префиксов в дереве, находящихся на расстоянии <= tolerance от заданного
   * префикса. Для коротких префиксов (длина < threshold) возвращает только точные совпадения.
   *
   * @param prefix Префикс для поиска
   *
   * @param tolerance Максимальное расстояние между строками
   *
   * @param threshold Пороговое значение длины строки, для которой применяется толерантность
   *
   * @param distanceFunction Функция расстояния между двумя строками
   *
   * @return Список префиксов, находящихся на расстоянии <= tolerance от заданного префикса
   *
   * @throws IllegalArgumentException Если prefix равен null или пустой строке ИЛИ tolerance меньше
   *         0 ИЛИ threshold меньше 0 ИЛИ distanceFunction равен null
   */
  public List<String> findSimilarPrefixes(String prefix, int tolerance, int threshold,
      BiFunction<String, String, Integer> distanceFunction) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("prefix cannot be null or empty");
    }
    if (tolerance < 0) {
      throw new IllegalArgumentException("tolerance cannot be negative");
    }
    if (threshold < 0) {
      throw new IllegalArgumentException("threshold cannot be negative");
    }
    if (distanceFunction == null) {
      throw new IllegalArgumentException("distanceFunction cannot be null");
    }

    List<String> result = new java.util.ArrayList<>();
    if (prefix.length() < threshold) {
      TrieNode node = getNode(prefix);
      if (node != null) {
        result.add(prefix);
      }
      return result;
    }
    collectSimilarPrefixes(root, new StringBuilder(), prefix, tolerance, distanceFunction, result);
    return result;
  }

  private void collectSimilarPrefixes(TrieNode node, StringBuilder current, String target,
      int tolerance, BiFunction<String, String, Integer> distanceFunction,
      java.util.List<String> result) {
    if (current.length() > 0 && node.getFrequency() > 0) {
      String candidate = current.toString();
      if (distanceFunction.apply(target, candidate) <= tolerance) {
        result.add(candidate);
      }
    }
    for (java.util.Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
      current.append(entry.getKey());
      collectSimilarPrefixes(entry.getValue(), current, target, tolerance, distanceFunction,
          result);
      current.deleteCharAt(current.length() - 1);
    }
  }
}
