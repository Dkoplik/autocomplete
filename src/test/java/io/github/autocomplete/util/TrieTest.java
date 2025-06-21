package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import io.github.autocomplete.model.WordFrequency;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrieTest {

  private Trie trie;

  @BeforeEach
  void setUp() {
    trie = new Trie();
  }

  @Test
  void insertAndGetFrequencyBasicOperations() {
    trie.insert("hello");
    trie.insert("hello");
    trie.insert("world");

    assertEquals(2, trie.getFrequency("hello"));
    assertEquals(1, trie.getFrequency("world"));
    assertEquals(0, trie.getFrequency("missing"));
  }

  @Test
  void insertEmptyStringThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> trie.insert(""));
  }

  @Test
  void removeSetsFrequencyToZero() {
    trie.insert("test");
    trie.insert("test");

    trie.remove("test");
    assertEquals(0, trie.getFrequency("test"));

    trie.insert("test");
    assertEquals(1, trie.getFrequency("test"));
  }

  @Test
  void removeTrulyDeleteRemovesWordAndPrunesNodes() {
    trie.insert("car");
    trie.insert("cart");
    trie.insert("carpet");
    trie.insert("cat");
    trie.insert("dog");

    // Remove 'carpet' with trulyDelete
    trie.remove("carpet", true);
    assertEquals(0, trie.getFrequency("carpet"));
    // Other words with shared prefix remain
    assertEquals(1, trie.getFrequency("car"));
    assertEquals(1, trie.getFrequency("cart"));
    assertEquals(1, trie.getFrequency("cat"));
    assertEquals(1, trie.getFrequency("dog"));

    // Remove 'cart' with trulyDelete
    trie.remove("cart", true);
    assertEquals(0, trie.getFrequency("cart"));
    assertEquals(1, trie.getFrequency("car"));
    assertEquals(1, trie.getFrequency("cat"));
    assertEquals(1, trie.getFrequency("dog"));

    // Remove 'car' with trulyDelete (should prune 'car' branch if no other words)
    trie.remove("car", true);
    assertEquals(0, trie.getFrequency("car"));
    assertEquals(1, trie.getFrequency("cat"));
    assertEquals(1, trie.getFrequency("dog"));

    // Remove 'cat' with trulyDelete
    trie.remove("cat", true);
    assertEquals(0, trie.getFrequency("cat"));
    assertEquals(1, trie.getFrequency("dog"));

    // Remove 'dog' with trulyDelete
    trie.remove("dog", true);
    assertEquals(0, trie.getFrequency("dog"));
    // Trie should now be empty
    assertTrue(trie.getAllWords().isEmpty());
  }

  @Test
  void removeTrulyDeleteReinsertAfterDeletion() {
    trie.insert("hello");
    trie.remove("hello", true);
    assertEquals(0, trie.getFrequency("hello"));
    trie.insert("hello");
    assertEquals(1, trie.getFrequency("hello"));
  }

  @Test
  void clearRemovesAllWords() {
    trie.insert("one");
    trie.insert("two");
    trie.insert("three");
    trie.clear();
    assertTrue(trie.getAllWords().isEmpty());
    assertEquals(0, trie.getFrequency("one"));
    assertEquals(0, trie.getFrequency("two"));
    assertEquals(0, trie.getFrequency("three"));
  }

  @Test
  void getAllWordsReturnsCompleteWordList() {
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
  void findCompletionsWithPrefixReturnsTopMatches() {
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("application");
    trie.insert("application");
    trie.insert("application");
    trie.insert("app");
    trie.insert("banana");

    List<WordFrequency> completions = trie.findCompletions("app", 10);
    assertEquals(3, completions.size());
    assertEquals("application", completions.get(0).word());
    assertEquals(3, completions.get(0).frequency());
    assertEquals("apple", completions.get(1).word());
    assertEquals(2, completions.get(1).frequency());
    assertEquals("app", completions.get(2).word());
    assertEquals(1, completions.get(2).frequency());

    List<WordFrequency> limited = trie.findCompletions("app", 2);
    assertEquals(2, limited.size());
    assertEquals("application", limited.get(0).word());
    assertEquals("apple", limited.get(1).word());
  }

  @Test
  void findCompletionsNonExistentPrefixReturnsEmptyList() {
    trie.insert("apple");
    trie.insert("banana");

    assertTrue(trie.findCompletions("xyz", 5).isEmpty());
  }

  @Test
  void findCompletionsEmptyPrefixThrowsException() {
    trie.insert("apple");
    trie.insert("banana");
    trie.insert("cherry");

    assertThrows(IllegalArgumentException.class, () -> trie.findCompletions("", 10));
  }

  @Test
  void getTopFrequentWordsReturnsHighestFrequencyWords() {
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
    List<WordFrequency> top3 = trie.getTopFrequentWords(3);
    assertEquals(3, top3.size());
    assertEquals("apple", top3.get(0).word());
    assertEquals(3, top3.get(0).frequency());
    assertEquals("banana", top3.get(1).word());
    assertEquals(3, top3.get(1).frequency());
    assertEquals("date", top3.get(2).word());
    assertEquals(2, top3.get(2).frequency());

    // Топ-1
    List<WordFrequency> top1 = trie.getTopFrequentWords(1);
    assertEquals(1, top1.size());
    assertEquals("apple", top1.get(0).word());
  }

  @Test
  void getTopFrequentWordsWithTiesOrdersAlphabetically() {
    trie.insert("apple");
    trie.insert("apple");
    trie.insert("banana");
    trie.insert("banana");
    trie.insert("cherry");

    List<WordFrequency> top = trie.getTopFrequentWords(10);
    assertEquals(3, top.size());
    assertEquals("apple", top.get(0).word());
    assertEquals("banana", top.get(1).word());
    assertEquals("cherry", top.get(2).word());
  }

  @Test
  void getTopFrequentWordsWithLimitLargerThanWordsReturnsAll() {
    trie.insert("a");
    trie.insert("b");

    List<WordFrequency> result = trie.getTopFrequentWords(10);
    assertEquals(2, result.size());
  }

  @Test
  void getTopFrequentWordsWithZeroOrNegativeLimitThrowsException() {
    trie.insert("apple");

    assertThrows(IllegalArgumentException.class, () -> trie.getTopFrequentWords(0));
    assertThrows(IllegalArgumentException.class, () -> trie.getTopFrequentWords(-1));
  }

  @Test
  void complexScenarioCombinedOperations() {
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

    List<WordFrequency> completions = trie.findCompletions("run", 10);
    assertEquals(3, completions.size()); // run, running, runs

    // runner не возвращается
    assertFalse(completions.stream().anyMatch(c -> "runner".equals(c.word())));

    List<WordFrequency> top = trie.getTopFrequentWords(2);
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
    trie.insert("Hello");
    trie.insert("hello");
    trie.insert("HELLO");

    assertEquals(1, trie.getFrequency("Hello"));
    assertEquals(1, trie.getFrequency("hello"));
    assertEquals(1, trie.getFrequency("HELLO"));

    List<WordFrequency> completions = trie.findCompletions("h", 10);
    assertEquals(1, completions.size());
    assertEquals("hello", completions.get(0).word());
  }

  @Test
  void saveAndLoadPreservesAllData() throws IOException {
    trie.insert("hello");
    trie.insert("world");
    trie.insert("hello");
    trie.insert("java");
    trie.insert("programming");

    Path tempFile = Files.createTempFile("trie", ".dat");
    File file = tempFile.toFile();

    try {
      trie.saveToFile(file);

      Trie loadedTrie = new Trie();
      loadedTrie.loadFromFile(file);

      // Проверяем что все данные сохранились
      assertEquals(trie.getFrequency("hello"), loadedTrie.getFrequency("hello"));
      assertEquals(trie.getFrequency("world"), loadedTrie.getFrequency("world"));
      assertEquals(trie.getFrequency("java"), loadedTrie.getFrequency("java"));
      assertEquals(trie.getFrequency("programming"), loadedTrie.getFrequency("programming"));

      // Проверяем что несуществующие слова возвращают 0
      assertEquals(0, loadedTrie.getFrequency("nonexistent"));

      // Проверяем топ слова
      List<WordFrequency> originalTop = trie.getTopFrequentWords(10);
      List<WordFrequency> loadedTop = loadedTrie.getTopFrequentWords(10);
      assertEquals(originalTop, loadedTop);

      // Проверяем автодополнения
      List<WordFrequency> originalCompletions = trie.findCompletions("h", 10);
      List<WordFrequency> loadedCompletions = loadedTrie.findCompletions("h", 10);
      assertEquals(originalCompletions, loadedCompletions);

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void constructorFromFileCreatesTrieWithLoadedData() throws IOException {
    trie.insert("apple");
    trie.insert("banana");
    trie.insert("apple");

    Path tempFile = Files.createTempFile("trie", ".dat");
    File file = tempFile.toFile();

    try {
      trie.saveToFile(file);

      Trie loadedTrie = new Trie(file);

      // Проверяем что данные загрузились
      assertEquals(2, loadedTrie.getFrequency("apple"));
      assertEquals(1, loadedTrie.getFrequency("banana"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveToFileNullFileThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> trie.saveToFile(null));
  }

  @Test
  void loadFromFileNullFileThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> trie.loadFromFile(null));
  }

  @Test
  void loadFromFileNonExistentFileThrowsException() {
    File nonExistentFile = new File("nonexistent.trie");
    assertThrows(IllegalArgumentException.class, () -> trie.loadFromFile(nonExistentFile));
  }

  @Test
  void loadFromFileInvalidFormatThrowsException() throws IOException {
    Path tempFile = Files.createTempFile("invalid", ".trie");
    File file = tempFile.toFile();

    try {
      // Записываем неверные данные
      Files.write(tempFile, "invalid data".getBytes());

      assertThrows(IllegalArgumentException.class, () -> trie.loadFromFile(file));
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveAndLoadEmptyTrieWorks() throws IOException {
    Trie emptyTrie = new Trie();

    Path tempFile = Files.createTempFile("empty", ".trie");
    File file = tempFile.toFile();

    try {
      emptyTrie.saveToFile(file);

      Trie loadedTrie = new Trie();
      loadedTrie.loadFromFile(file);

      // Проверяем что trie остался пустым
      assertEquals(0, loadedTrie.getAllWords().size());
      assertEquals(0, loadedTrie.getTopFrequentWords(10).size());

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveAndLoadComplexStructureWorks() throws IOException {
    trie.insert("cat");
    trie.insert("car");
    trie.insert("card");
    trie.insert("care");
    trie.insert("careful");
    trie.insert("carefully");
    trie.insert("careless");
    trie.insert("careless");

    Path tempFile = Files.createTempFile("complex", ".trie");
    File file = tempFile.toFile();

    try {
      trie.saveToFile(file);

      Trie loadedTrie = new Trie();
      loadedTrie.loadFromFile(file);

      // Проверяем что все данные сохранились
      Map<String, Integer> originalWords = trie.getAllWords();
      Map<String, Integer> loadedWords = loadedTrie.getAllWords();
      assertEquals(originalWords, loadedWords);

      // Проверяем автодополнения для разных префиксов
      List<WordFrequency> originalCaCompletions = trie.findCompletions("ca", 10);
      List<WordFrequency> loadedCaCompletions = loadedTrie.findCompletions("ca", 10);
      assertEquals(originalCaCompletions, loadedCaCompletions);

      List<WordFrequency> originalCareCompletions = trie.findCompletions("care", 10);
      List<WordFrequency> loadedCareCompletions = loadedTrie.findCompletions("care", 10);
      assertEquals(originalCareCompletions, loadedCareCompletions);

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveAndLoadWithUnicodeCharactersWorks() throws IOException {
    // Тестируем с Unicode символами
    trie.insert("café");
    trie.insert("résumé");
    trie.insert("naïve");
    trie.insert("naïve");
    trie.insert("привет");
    trie.insert("こんにちは");

    Path tempFile = Files.createTempFile("unicode", ".trie");
    File file = tempFile.toFile();

    try {
      trie.saveToFile(file);

      Trie loadedTrie = new Trie();
      loadedTrie.loadFromFile(file);

      // Проверяем что все Unicode данные сохранились
      assertEquals(1, loadedTrie.getFrequency("café"));
      assertEquals(1, loadedTrie.getFrequency("résumé"));
      assertEquals(2, loadedTrie.getFrequency("naïve"));
      assertEquals(1, loadedTrie.getFrequency("привет"));
      assertEquals(1, loadedTrie.getFrequency("こんにちは"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
