package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import io.github.autocomplete.util.Candidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AutocompleteProviderTest {

  private TextAnalyzer textAnalyzer;
  private AutocompleteProvider provider;

  @BeforeEach
  void setUp() {
    textAnalyzer = new TextAnalyzer();
    provider = new AutocompleteProvider(textAnalyzer);
  }

  @Test
  void getAutocomplete_WithValidPrefix_ReturnsCompletions() {
    provider.addText("application apple applet");

    List<Candidate> completions = provider.getAutocomplete("app", 10);
    assertEquals(3, completions.size());
    assertEquals("apple", completions.get(0).word());
  }

  @Test
  void getAutocomplete_WithCache_ReturnsCachedResults() {
    provider = new AutocompleteProvider(textAnalyzer, 10);
    provider.addText("application apple applet");

    // заполнение кеша
    List<Candidate> firstCall = provider.getAutocomplete("app", 5);
    // получение из кеша
    List<Candidate> secondCall = provider.getAutocomplete("app", 5);

    assertEquals(firstCall, secondCall);
  }

  @Test
  void getAutocomplete_WithoutCache_AlwaysCalculates() {
    provider = new AutocompleteProvider(textAnalyzer, 0); // Без кеша
    provider.addText("application apple applet");

    List<Candidate> firstCall = provider.getAutocomplete("app", 5);
    List<Candidate> secondCall = provider.getAutocomplete("app", 5);

    assertNotSame(firstCall, secondCall); // Не один и тот же объект
    assertEquals(firstCall, secondCall);
  }

  void getAutocomplete_InvalidPrefix_ThrowsException() {
    String prefix = "";
    assertThrows(IllegalArgumentException.class, () -> provider.getAutocomplete(prefix, 5));
  }

  @Test
  void getAutocomplete_NullPrefix_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> provider.getAutocomplete(null, 5));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -10})
  void getAutocomplete_InvalidLimit_ThrowsException(int limit) {
    assertThrows(IllegalArgumentException.class, () -> provider.getAutocomplete("valid", limit));
  }

  @Test
  void processText_UpdatesUnderlyingData() {
    provider.addText("apple application");
    List<Candidate> initial = provider.getAutocomplete("app", 10);
    assertEquals(2, initial.size());

    provider.addText("applet approval");
    List<Candidate> updated = provider.getAutocomplete("app", 10);
    assertEquals(4, updated.size());
  }

  @Test
  void cache_DoesInvalidateAfterProcessText() {
    provider = new AutocompleteProvider(textAnalyzer, 10);
    provider.addText("apple application");

    List<Candidate> cachedResult = provider.getAutocomplete("app", 10);
    provider.addText("applet approval");

    List<Candidate> newResult = provider.getAutocomplete("app", 10);
    assertNotSame(cachedResult, newResult);
    assertEquals(4, newResult.size());
  }

  @Test
  void cache_RespectsSizeLimit() {
    provider = new AutocompleteProvider(textAnalyzer, 2);
    provider.addText("apple application");

    List<Candidate> appCall = provider.getAutocomplete("app", 5);
    provider.getAutocomplete("ban", 5);
    provider.getAutocomplete("cher", 5); // Должен вытеснить app

    List<Candidate> appCall2 = provider.getAutocomplete("app", 5);
    assertNotSame(appCall, appCall2);
  }

  @Test
  void complexWorkflow_WithCache() {
    provider = new AutocompleteProvider(textAnalyzer, 5);

    provider.addText("java javascript python");
    List<Candidate> javaCompletions1 = provider.getAutocomplete("jav", 5);
    assertEquals(2, javaCompletions1.size());

    provider.addText("javafx javelin");
    List<Candidate> javaCompletions2 = provider.getAutocomplete("jav", 5);

    assertNotSame(javaCompletions1, javaCompletions2);
    assertEquals(4, javaCompletions2.size());
  }

  @Test
  void zeroCacheSize_DisablesCaching() {
    provider = new AutocompleteProvider(textAnalyzer, 0);
    provider.addText("test tests");

    List<Candidate> firstCall = provider.getAutocomplete("test", 5);
    List<Candidate> secondCall = provider.getAutocomplete("test", 5);

    assertNotSame(firstCall, secondCall);
    assertEquals(firstCall, secondCall);
  }

  @Test
  void negativeCacheSize_ThrowsExcpetion() {
    assertThrows(IllegalArgumentException.class, () -> new AutocompleteProvider(textAnalyzer, -1));
  }

  @Test
  void getAutocomplete_TypoTolerance_SuggestsSimilar() {
    AutocompleteConfig config = new AutocompleteConfig(io.github.autocomplete.util.Levenshtein::distance, 1, 1, 0.5, 1.0);
    provider = new AutocompleteProvider(textAnalyzer, config, 10);
    provider.addText("apple ample apply");
    List<Candidate> completions = provider.getAutocomplete("aple", 10);
    assertTrue(completions.stream().anyMatch(c -> c.word().equals("apple")));
    assertTrue(completions.stream().anyMatch(c -> c.word().equals("ample")));
    assertFalse(completions.stream().anyMatch(c -> c.word().equals("apply")));
  }

  @Test
  void getAutocomplete_TypoTolerance_Threshold() {
    AutocompleteConfig config = new AutocompleteConfig(io.github.autocomplete.util.Levenshtein::distance, 5, 1, 0.5, 1.0);
    provider = new AutocompleteProvider(textAnalyzer, config, 10);
    provider.addText("apple ample apply");
    List<Candidate> completions = provider.getAutocomplete("app", 10);
    assertEquals(2, completions.size());
    completions = provider.getAutocomplete("applz", 10);
    assertTrue(completions.stream().anyMatch(c -> c.word().equals("apple")));
  }

  @Test
  void getAutocomplete_TypoTolerance_Weights() {
    AutocompleteConfig config = new AutocompleteConfig(io.github.autocomplete.util.Levenshtein::distance, 1, 1, 0.1, 1.0);
    provider = new AutocompleteProvider(textAnalyzer, config, 10);
    provider.addText("apple apple ample");
    List<Candidate> completions = provider.getAutocomplete("aple", 10);
    Candidate apple = completions.stream().filter(c -> c.word().equals("apple")).findFirst().orElseThrow();
    Candidate ample = completions.stream().filter(c -> c.word().equals("ample")).findFirst().orElseThrow();
    assertTrue(apple.weight() > ample.weight());
  }

  @Test
  void getAutocomplete_Cache_ReusesLargerLimit() {
    provider.addText("apple application applet");
    List<Candidate> big = provider.getAutocomplete("app", 10);
    List<Candidate> small = provider.getAutocomplete("app", 2);
    assertEquals(big.subList(0, 2), small);
  }

  @Test
  void getAutocomplete_Cache_DifferentLimitMiss() {
    provider.addText("apple application applet");
    List<Candidate> small = provider.getAutocomplete("app", 2);
    List<Candidate> big = provider.getAutocomplete("app", 10);
    // Should not be same object, as big is recomputed
    assertNotSame(small, big);
    assertEquals(small, big.subList(0, 2));
  }

  @Test
  void getAutocomplete_Cache_SimilarPrefixCache() {
    AutocompleteConfig config = new AutocompleteConfig(io.github.autocomplete.util.Levenshtein::distance, 1, 1, 0.5, 1.0);
    provider = new AutocompleteProvider(textAnalyzer, config, 10);
    provider.addText("apple ample apply");
    // First call populates similarPrefixCache
    provider.getAutocomplete("aple", 10);
    // Second call should hit similarPrefixCache (no assertion, but should not throw or recompute)
    provider.getAutocomplete("aple", 10);
  }
}
