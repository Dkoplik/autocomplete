package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import io.github.autocomplete.config.TokenizerConfig;
import io.github.autocomplete.model.WordFrequency;
import io.github.autocomplete.tokenizer.SimpleTokenizer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextAnalyzerTest {

  private TextAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new TextAnalyzer();
  }

  @Test
  void processTextWithDefaultTokenizerUpdatesWordFrequencies() {
    analyzer.addText("Apple apple Banana apple Cherry");

    assertEquals(3, analyzer.getWordFrequency("apple"));
    assertEquals(1, analyzer.getWordFrequency("banana"));
    assertEquals(1, analyzer.getWordFrequency("cherry"));
  }

  @Test
  void processTextWithCustomTokenizerRespectsConfiguration() {
    TokenizerConfig config =
        new TokenizerConfig("\\s+", c -> Character.isLetter(c) || c == '-', false);
    SimpleTokenizer tokenizer = new SimpleTokenizer();
    tokenizer.setConfig(config);

    TextAnalyzer customAnalyzer = new TextAnalyzer(tokenizer);
    customAnalyzer.addText("e-mail user-name 123 test!");

    assertEquals(1, customAnalyzer.getWordFrequency("e-mail"));
    assertEquals(1, customAnalyzer.getWordFrequency("user-name"));
    assertEquals(0, customAnalyzer.getWordFrequency("123"));
    assertEquals(1, customAnalyzer.getWordFrequency("test"));
  }

  @Test
  void getWordFrequencyForNonExistentWordReturnsZero() {
    analyzer.addText("test");
    assertEquals(0, analyzer.getWordFrequency("missing"));
  }

  @Test
  void getTopWordsReturnsHighestFrequencyWords() {
    analyzer.addText("apple banana apple cherry apple banana");

    List<WordFrequency> topWords = analyzer.getTopWords(2);
    assertEquals(2, topWords.size());
    assertEquals("apple", topWords.get(0).word());
    assertEquals("banana", topWords.get(1).word());
  }

  @Test
  void getAllWordsReturnsCompleteWordMap() {
    analyzer.addText("apple apple banana");

    Map<String, Integer> allWords = analyzer.getAllWords();
    assertEquals(2, allWords.size());
    assertEquals(2, allWords.get("apple"));
    assertEquals(1, allWords.get("banana"));
  }

  @Test
  void removeWordSetsFrequencyToZero() {
    analyzer.addText("test test");
    analyzer.removeWord("test");

    assertEquals(0, analyzer.getWordFrequency("test"));
    assertFalse(analyzer.getAllWords().containsKey("test"));
  }

  @Test
  void getWordsByRegexFiltersWordsCorrectly() {
    analyzer.addText("apple applet application banana bandana cherry");

    // Все слова, начинающиеся на "app"
    Map<String, Integer> appWords = analyzer.getWordsByRegex("app.*");
    assertEquals(3, appWords.size());
    assertTrue(appWords.containsKey("apple"));
    assertTrue(appWords.containsKey("applet"));
    assertTrue(appWords.containsKey("application"));

    // Слова из 6 букв
    Map<String, Integer> sixLetterWords = analyzer.getWordsByRegex(".{6}");
    assertEquals(3, sixLetterWords.size());
    assertTrue(sixLetterWords.containsKey("banana"));
    assertTrue(sixLetterWords.containsKey("cherry"));
    assertTrue(sixLetterWords.containsKey("applet"));
  }

  @Test
  void getWordsByRegexWithNoMatchesReturnsEmptyMap() {
    analyzer.addText("apple banana");
    assertTrue(analyzer.getWordsByRegex("xyz.*").isEmpty());
  }

  @Test
  void complexWorkflowCombinedOperations() {
    analyzer.addText("The quick brown fox jumps over the lazy dog");
    analyzer.addText("The quick dog jumps over the lazy fox");

    assertEquals(4, analyzer.getWordFrequency("the"));
    assertEquals(2, analyzer.getWordFrequency("quick"));
    assertEquals(2, analyzer.getWordFrequency("jumps"));
    assertEquals(2, analyzer.getWordFrequency("over"));
    assertEquals(2, analyzer.getWordFrequency("lazy"));
    assertEquals(1, analyzer.getWordFrequency("brown"));
    assertEquals(2, analyzer.getWordFrequency("fox"));

    analyzer.removeWord("the");
    assertEquals(0, analyzer.getWordFrequency("the"));

    // Получение топ-3 слов
    List<WordFrequency> top3 = analyzer.getTopWords(3);
    assertEquals(3, top3.size());
    assertEquals("dog", top3.get(0).word());
    assertEquals("fox", top3.get(1).word());
    assertEquals("jumps", top3.get(2).word());

    // Поиск по regex
    Map<String, Integer> fourLetterWords = analyzer.getWordsByRegex(".{4}");
    assertEquals(2, fourLetterWords.size());
    assertTrue(fourLetterWords.containsKey("over"));
    assertTrue(fourLetterWords.containsKey("lazy"));
  }

  @Test
  void caseSensitivityWithDefaultTokenizer() {
    analyzer.addText("Apple apple APPLE");

    assertEquals(3, analyzer.getWordFrequency("apple"));
    assertEquals(0, analyzer.getWordFrequency("Apple"));

    List<WordFrequency> top = analyzer.getTopWords(1);
    assertEquals("apple", top.get(0).word());
  }

  @Test
  void emptyTextProcessingDoesNotChangeState() {
    analyzer.addText("");
    assertTrue(analyzer.getAllWords().isEmpty());

    analyzer.addText("  \n\t");
    assertTrue(analyzer.getAllWords().isEmpty());
  }

  @Test
  void getTokenizerReturnsConfiguredTokenizer() {
    SimpleTokenizer customTokenizer = new SimpleTokenizer();
    TextAnalyzer customAnalyzer = new TextAnalyzer(customTokenizer);

    assertSame(customTokenizer, customAnalyzer.getTokenizer());
  }

  @Test
  void specialCharactersHandling() {
    String text = "café résumé naïve naïve";
    analyzer.addText(text);

    assertEquals(1, analyzer.getWordFrequency("café"));
    assertEquals(1, analyzer.getWordFrequency("résumé"));
    assertEquals(2, analyzer.getWordFrequency("naïve"));
  }

  @Test
  void saveAndLoadPreservesAllData() throws IOException {
    // Подготавливаем данные
    String text1 = "hello world hello java";
    String text2 = "java programming world";
    analyzer.addText(text1);
    analyzer.addText(text2);

    // Сохраняем в временный файл
    Path tempFile = Files.createTempFile("analyzer", ".trie");
    File file = tempFile.toFile();

    try {
      analyzer.saveToFile(file);

      // Создаём новый анализатор и загружаем данные
      TextAnalyzer loadedAnalyzer = new TextAnalyzer();
      loadedAnalyzer.loadFromFile(file);

      // Проверяем что все данные сохранились
      assertEquals(analyzer.getWordFrequency("hello"), loadedAnalyzer.getWordFrequency("hello"));
      assertEquals(analyzer.getWordFrequency("world"), loadedAnalyzer.getWordFrequency("world"));
      assertEquals(analyzer.getWordFrequency("java"), loadedAnalyzer.getWordFrequency("java"));
      assertEquals(analyzer.getWordFrequency("programming"),
          loadedAnalyzer.getWordFrequency("programming"));

      // Проверяем что несуществующие слова возвращают 0
      assertEquals(0, loadedAnalyzer.getWordFrequency("nonexistent"));

      // Проверяем топ слова
      List<WordFrequency> originalTop = analyzer.getTopWords(10);
      List<WordFrequency> loadedTop = loadedAnalyzer.getTopWords(10);
      assertEquals(originalTop, loadedTop);

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveToFileNullFileThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> analyzer.saveToFile(null));
  }

  @Test
  void loadFromFileNullFileThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> analyzer.loadFromFile(null));
  }

  @Test
  void loadFromFileNonExistentFileThrowsException() {
    File nonExistentFile = new File("nonexistent.trie");
    assertThrows(IllegalArgumentException.class, () -> analyzer.loadFromFile(nonExistentFile));
  }

  @Test
  void loadFromFileInvalidFormatThrowsException() throws IOException {
    Path tempFile = Files.createTempFile("invalid", ".trie");
    File file = tempFile.toFile();

    try {
      // Записываем неверные данные
      Files.write(tempFile, "invalid data".getBytes());

      assertThrows(IllegalArgumentException.class, () -> analyzer.loadFromFile(file));
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveAndLoadEmptyAnalyzerWorks() throws IOException {
    TextAnalyzer emptyAnalyzer = new TextAnalyzer();

    Path tempFile = Files.createTempFile("empty", ".trie");
    File file = tempFile.toFile();

    try {
      emptyAnalyzer.saveToFile(file);

      TextAnalyzer loadedAnalyzer = new TextAnalyzer();
      loadedAnalyzer.loadFromFile(file);

      // Проверяем что анализатор остался пустым
      assertEquals(0, loadedAnalyzer.getAllWords().size());
      assertEquals(0, loadedAnalyzer.getTopWords(10).size());

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void saveAndLoadLargeDatasetWorks() throws IOException {
    StringBuilder largeText = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeText.append("word").append(i).append(" ");
      if (i % 10 == 0) {
        largeText.append("common ");
      }
    }

    analyzer.addText(largeText.toString());

    Path tempFile = Files.createTempFile("large", ".trie");
    File file = tempFile.toFile();

    try {
      analyzer.saveToFile(file);

      TextAnalyzer loadedAnalyzer = new TextAnalyzer();
      loadedAnalyzer.loadFromFile(file);

      // Проверяем что все данные сохранились
      Map<String, Integer> originalWords = analyzer.getAllWords();
      Map<String, Integer> loadedWords = loadedAnalyzer.getAllWords();
      assertEquals(originalWords, loadedWords);

      assertEquals(100, loadedAnalyzer.getWordFrequency("common"));
      assertEquals(1000, loadedAnalyzer.getWordFrequency("word"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
