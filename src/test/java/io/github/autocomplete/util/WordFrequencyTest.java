package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class WordFrequencyTest {
  @Test
  void compareTo_WhenFrequenciesDiffer_ReturnsCorrectComparison() {
    WordFrequency highFreq = new WordFrequency("test", 10);
    WordFrequency lowFreq = new WordFrequency("test", 5);
    assertTrue(highFreq.compareTo(lowFreq) > 0);
    assertTrue(lowFreq.compareTo(highFreq) < 0);
  }

  @ParameterizedTest
  @CsvSource({"apple, banana, 10, 10, 1", "banana, apple, 10, 10, -1", "apple, apple, 10, 10, 0"})
  void compareTo_WhenFrequenciesEqual_ComparesWords(String word1, String word2, int freq1,
      int freq2, int expected) {
    WordFrequency w1 = new WordFrequency(word1, freq1);
    WordFrequency w2 = new WordFrequency(word2, freq2);
    assertEquals(expected, w1.compareTo(w2));
  }

  @Test
  void compareTo_WithNull_ThrowsNullPointerException() {
    WordFrequency wf = new WordFrequency("test", 5);
    assertThrows(NullPointerException.class, () -> wf.compareTo(null));
  }

  @Test
  void constructor_WithNegativeFrequency_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new WordFrequency("test", -1));
  }

  @Test
  void record_Accessors_ReturnCorrectValues() {
    WordFrequency wf = new WordFrequency("word", 42);
    assertEquals("word", wf.word());
    assertEquals(42, wf.frequency());
  }
}
