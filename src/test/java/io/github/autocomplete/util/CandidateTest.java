package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CandidateTest {
  @Test
  void compareTo_WhenWeightsDiffer_ReturnsCorrectComparison() {
    Candidate highWeight = new Candidate("test", 10);
    Candidate lowWeight = new Candidate("test", 5);
    assertTrue(highWeight.compareTo(lowWeight) > 0);
    assertTrue(lowWeight.compareTo(highWeight) < 0);
  }

  @ParameterizedTest
  @CsvSource({"apple, banana, 10, 10, 1", "banana, apple, 10, 10, -1", "apple, apple, 10, 10, 0"})
  void compareTo_WhenWeightsEqual_ComparesWords(String word1, String word2, int weight1,
      int weight2, int expected) {
    Candidate c1 = new Candidate(word1, weight1);
    Candidate c2 = new Candidate(word2, weight2);
    assertEquals(expected, c1.compareTo(c2));
  }

  @Test
  void compareTo_WithNull_ThrowsNullPointerException() {
    Candidate candidate = new Candidate("test", 5);
    assertThrows(NullPointerException.class, () -> candidate.compareTo(null));
  }

  @Test
  void constructor_WithNegativeWeight_IsAllowed() {
    assertDoesNotThrow(() -> new Candidate("test", -1));
  }

  @Test
  void record_Accessors_ReturnCorrectValues() {
    Candidate candidate = new Candidate("word", 42);
    assertEquals("word", candidate.word());
    assertEquals(42, candidate.weight());
  }
}
