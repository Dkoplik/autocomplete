package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class WordFrequencyTest {
  @Test
  void compareToWhenFrequenciesDifferReturnsCorrectComparison() {
    WordFrequency highFreq = new WordFrequency("test", 10);
    WordFrequency lowFreq = new WordFrequency("test", 5);
    assertTrue(highFreq.compareTo(lowFreq) > 0);
    assertTrue(lowFreq.compareTo(highFreq) < 0);
  }

  @ParameterizedTest
  @CsvSource({"apple, banana, 10, 10, 1", "banana, apple, 10, 10, -1", "apple, apple, 10, 10, 0"})
  void compareToWhenFrequenciesEqualComparesWords(String word1, String word2, int freq1,
      int freq2, int expected) {
    WordFrequency w1 = new WordFrequency(word1, freq1);
    WordFrequency w2 = new WordFrequency(word2, freq2);
    assertEquals(expected, w1.compareTo(w2));
  }

  @Test
  void compareToWithNullThrowsNullPointerException() {
    WordFrequency wf = new WordFrequency("test", 5);
    assertThrows(NullPointerException.class, () -> wf.compareTo(null));
  }

  @Test
  void constructorWithNegativeFrequencyThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new WordFrequency("test", -1));
  }

  @Test
  void recordAccessorsReturnCorrectValues() {
    WordFrequency wf = new WordFrequency("word", 42);
    assertEquals("word", wf.word());
    assertEquals(42, wf.frequency());
  }
}
