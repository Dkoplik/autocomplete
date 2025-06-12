package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import io.github.autocomplete.util.Candidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class TrieTest {

  private Trie trie;

  @BeforeEach
  void setUp() {
    trie = new Trie();
  }

  @Test
  void insert_AndGetFrequency_BasicOperations() {
    trie.insert("hello");
    trie.insert("hello");
    trie.insert("world");

    assertEquals(2, trie.getFrequency("hello"));
    assertEquals(1, trie.getFrequency("world"));
    assertEquals(0, trie.getFrequency("missing"));
  }

  @Test
  void insert_EmptyString_HandledCorrectly() {
    trie.insert("");
    assertEquals(1, trie.getFrequency(""));
  }

  @Test
  void remove_SetsFrequencyToZero() {
    trie.insert("test");
    trie.insert("test");

    trie.remove("test");
    assertEquals(0, trie.getFrequency("test"));

    trie.insert("test");
    assertEquals(1, trie.getFrequency("test"));
  }

  @Test
  void getAllWords_ReturnsCompleteWordList() {
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("app");
    trie.insert("banana");
    trie.insert("banana");
    trie.insert("banana");
    trie.insert("cherry");

    Map<String, Integer> words = trie.getAllWords();

    assertEquals(4, words.size());
    assertEquals(2, words.get("apple"));
    assertEquals(3, words.get("banana"));
    assertEquals(1, words.get("cherry"));
  }

  @Test
  void findCompletions_WithPrefix_ReturnsTopMatches() {
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("application");
    trie.insert("application");
    trie.insert("application");
    trie.insert("app");
    trie.insert("banana");

    List<Candidate> completions = trie.findCompletions("app", 10);
    assertEquals(3, completions.size());
    assertEquals("application", completions.get(0).word());
    assertEquals(3, completions.get(0).frequency());
    assertEquals("apple", completions.get(1).word());
    assertEquals(2, completions.get(1).frequency());
    assertEquals("app", completions.get(2).word());
    assertEquals(1, completions.get(2).frequency());

    List<Candidate> limited = trie.findCompletions("app", 2);
    assertEquals(2, limited.size());
    assertEquals("application", limited.get(0).word());
    assertEquals("apple", limited.get(1).word());
  }

  @Test
  void findCompletions_NonExistentPrefix_ReturnsEmptyList() {
    trie.insert("apple");
    trie.insert("banana");

    assertTrue(trie.findCompletions("xyz", 5).isEmpty());
  }

  @Test
  void findCompletions_EmptyPrefix_ReturnsAllWords() {
    trie.insert("apple");
    trie.insert("banana");
    trie.insert("cherry");

    List<Candidate> completions = trie.findCompletions("", 10);
    assertEquals(3, completions.size());
  }

  @Test
  void getTopFrequentWords_ReturnsHighestFrequencyWords() {
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("banana");
    trie.insert("banana");
    trie.insert("banana");
    trie.insert("cherry");
    trie.insert("date");
    trie.insert("date");
    trie.insert("elderberry");

    // Топ-3
    List<Candidate> top3 = trie.getTopFrequentWords(3);
    assertEquals(3, top3.size());
    assertEquals("apple", top3.get(0).word());
    assertEquals(3, top3.get(0).frequency());
    assertEquals("banana", top3.get(1).word());
    assertEquals(3, top3.get(1).frequency());
    assertEquals("date", top3.get(2).word());
    assertEquals(2, top3.get(2).frequency());

    // Топ-1
    List<Candidate> top1 = trie.getTopFrequentWords(1);
    assertEquals(1, top1.size());
    assertEquals("apple", top1.get(0).word());
  }

  @Test
  void getTopFrequentWords_WithTies_OrdersAlphabetically() {
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("banana");
    trie.insert("banana");
    trie.insert("cherry");

    List<Candidate> top = trie.getTopFrequentWords(10);
    assertEquals(3, top.size());
    assertEquals("apple", top.get(0).word());
    assertEquals("banana", top.get(1).word());
    assertEquals("cherry", top.get(2).word());
  }

  @Test
  void getTopFrequentWords_WithLimitLargerThanWords_ReturnsAll() {
    trie.insert("a");
    trie.insert("b");

    List<Candidate> result = trie.getTopFrequentWords(10);
    assertEquals(2, result.size());
  }

  @Test
  void getTopFrequentWords_WithZeroOrNegativeLimit_ReturnsEmptyList() {
    trie.insert("apple");

    assertTrue(trie.getTopFrequentWords(0).isEmpty());
    assertTrue(trie.getTopFrequentWords(-1).isEmpty());
  }

  @Test
  void complexScenario_CombinedOperations() {
    trie.insert("run");
    trie.insert("runner");
    trie.insert("running");
    trie.insert("ran");
    trie.insert("runs");
    trie.insert("run");

    assertEquals(2, trie.getFrequency("run"));
    assertEquals(1, trie.getFrequency("runner"));
    assertEquals(1, trie.getFrequency("running"));
    assertEquals(1, trie.getFrequency("ran"));
    assertEquals(1, trie.getFrequency("runs"));

    trie.remove("runner");
    assertEquals(0, trie.getFrequency("runner"));

    List<Candidate> completions = trie.findCompletions("run", 10);
    assertEquals(3, completions.size()); // run, running, runs

    // runner не возвращается
    assertFalse(completions.stream().anyMatch(c -> "runner".equals(c.word())));

    List<Candidate> top = trie.getTopFrequentWords(2);
    assertEquals(2, top.size());
    assertEquals("run", top.get(0).word());
    assertEquals(2, top.get(0).frequency());

    // Следующее может быть любым из слов с частотой 1, но проверяем что они есть
    assertTrue(top.stream().anyMatch(c -> c.frequency() == 1));

    Map<String, Integer> allWords = trie.getAllWords();
    assertEquals(4, allWords.size());
    assertEquals(2, allWords.get("run"));
    assertEquals(1, allWords.get("running"));
    assertEquals(1, allWords.get("ran"));
    assertEquals(1, allWords.get("runs"));
  }

  @Test
  void caseSensitiveBehavior() {
    trie.insert("Apple");
    trie.insert("apple");
    trie.insert("APPLE");

    assertEquals(1, trie.getFrequency("Apple"));
    assertEquals(1, trie.getFrequency("apple"));
    assertEquals(1, trie.getFrequency("APPLE"));

    List<Candidate> completions = trie.findCompletions("a", 10);
    assertEquals(1, completions.size());
    assertEquals("apple", completions.get(0).word());
  }
}
