package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import io.github.autocomplete.tokenizer.SimpleTokenizer;
import io.github.autocomplete.tokenizer.TokenizerConfig;
import io.github.autocomplete.util.Candidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextAnalyzerTest {

  private TextAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new TextAnalyzer();
  }

  @Test
  void processText_WithDefaultTokenizer_UpdatesWordFrequencies() {
    analyzer.processText("Apple apple Banana apple Cherry");

    assertEquals(3, analyzer.getWordFrequency("apple"));
    assertEquals(1, analyzer.getWordFrequency("banana"));
    assertEquals(1, analyzer.getWordFrequency("cherry"));
  }

  @Test
  void processText_WithCustomTokenizer_RespectsConfiguration() {
    TokenizerConfig config =
        new TokenizerConfig("\\s+", c -> Character.isLetter(c) || c == '-', false);
    SimpleTokenizer tokenizer = new SimpleTokenizer();
    tokenizer.setConfig(config);

    TextAnalyzer customAnalyzer = new TextAnalyzer(tokenizer);
    customAnalyzer.processText("e-mail user-name 123 test!");

    assertEquals(1, customAnalyzer.getWordFrequency("e-mail"));
    assertEquals(1, customAnalyzer.getWordFrequency("user-name"));
    assertEquals(0, customAnalyzer.getWordFrequency("123"));
    assertEquals(1, customAnalyzer.getWordFrequency("test"));
  }

  @Test
  void getWordFrequency_ForNonExistentWord_ReturnsZero() {
    analyzer.processText("test");
    assertEquals(0, analyzer.getWordFrequency("missing"));
  }

  @Test
  void getTopWords_ReturnsHighestFrequencyWords() {
    analyzer.processText("apple banana apple cherry apple banana");

    List<Candidate> topWords = analyzer.getTopWords(2);
    assertEquals(2, topWords.size());
    assertEquals("apple", topWords.get(0).word());
    assertEquals(3, topWords.get(0).frequency());
    assertEquals("banana", topWords.get(1).word());
    assertEquals(2, topWords.get(1).frequency());
  }

  @Test
  void getAllWords_ReturnsCompleteWordMap() {
    analyzer.processText("apple apple banana");

    Map<String, Integer> allWords = analyzer.getAllWords();
    assertEquals(2, allWords.size());
    assertEquals(2, allWords.get("apple"));
    assertEquals(1, allWords.get("banana"));
  }

  @Test
  void removeWord_SetsFrequencyToZero() {
    analyzer.processText("test test");
    analyzer.removeWord("test");

    assertEquals(0, analyzer.getWordFrequency("test"));
    assertFalse(analyzer.getAllWords().containsKey("test"));
  }

  @Test
  void getWordsByRegex_FiltersWordsCorrectly() {
    analyzer.processText("apple applet application banana bandana cherry");

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
  void getWordsByRegex_WithNoMatches_ReturnsEmptyMap() {
    analyzer.processText("apple banana");
    assertTrue(analyzer.getWordsByRegex("xyz.*").isEmpty());
  }

  @Test
  void complexWorkflow_CombinedOperations() {
    analyzer.processText("The quick brown fox jumps over the lazy dog");
    analyzer.processText("The quick dog jumps over the lazy fox");

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
    List<Candidate> top3 = analyzer.getTopWords(3);
    assertEquals(3, top3.size());
    assertEquals("dog", top3.get(0).word());
    assertEquals(2, top3.get(0).frequency());
    assertEquals("fox", top3.get(1).word());
    assertEquals(2, top3.get(1).frequency());
    assertEquals("jumps", top3.get(2).word());
    assertEquals(2, top3.get(2).frequency());

    // Поиск по regex
    Map<String, Integer> fourLetterWords = analyzer.getWordsByRegex(".{4}");
    assertEquals(2, fourLetterWords.size());
    assertTrue(fourLetterWords.containsKey("over"));
    assertTrue(fourLetterWords.containsKey("lazy"));
  }

  @Test
  void caseSensitivity_WithDefaultTokenizer() {
    analyzer.processText("Apple apple APPLE");

    assertEquals(3, analyzer.getWordFrequency("apple"));
    assertEquals(0, analyzer.getWordFrequency("Apple"));

    List<Candidate> top = analyzer.getTopWords(1);
    assertEquals("apple", top.get(0).word());
    assertEquals(3, top.get(0).frequency());
  }

  @Test
  void emptyTextProcessing_DoesNotChangeState() {
    analyzer.processText("");
    assertTrue(analyzer.getAllWords().isEmpty());

    analyzer.processText("  \n\t");
    assertTrue(analyzer.getAllWords().isEmpty());
  }

  @Test
  void getTokenizer_ReturnsConfiguredTokenizer() {
    SimpleTokenizer customTokenizer = new SimpleTokenizer();
    TextAnalyzer customAnalyzer = new TextAnalyzer(customTokenizer);

    assertSame(customTokenizer, customAnalyzer.getTokenizer());
  }

  @Test
  void specialCharactersHandling() {
    analyzer.processText("hello-world email@domain.com 123-456");

    assertEquals(1, analyzer.getWordFrequency("helloworld"));
    assertEquals(0, analyzer.getWordFrequency("email"));
    assertEquals(0, analyzer.getWordFrequency("123"));
  }

  @Test
  void getCompletions_InternalMethodWorks() {
    analyzer.processText("application apple applet");

    List<Candidate> completions = analyzer.getCompletions("app", 10);
    assertEquals(3, completions.size());
    assertEquals("apple", completions.get(0).word());
  }
}
