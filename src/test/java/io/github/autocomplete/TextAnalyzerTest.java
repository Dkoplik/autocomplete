package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import io.github.autocomplete.tokenizer.SimpleTokenizer;
import io.github.autocomplete.tokenizer.TokenizerConfig;
import io.github.autocomplete.util.WordFrequency;
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
    analyzer.addText("hello-world email@domain.com 123-456");

    assertEquals(1, analyzer.getWordFrequency("helloworld"));
    assertEquals(0, analyzer.getWordFrequency("email"));
    assertEquals(0, analyzer.getWordFrequency("123"));
  }
}
