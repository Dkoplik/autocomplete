package io.github.autocomplete.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CandidateTest {
  @Test
  void compareToWhenWeightsDifferReturnsCorrectComparison() {
    Candidate highWeight = new Candidate("test", 10);
    Candidate lowWeight = new Candidate("test", 5);
    assertTrue(highWeight.compareTo(lowWeight) > 0);
    assertTrue(lowWeight.compareTo(highWeight) < 0);
  }

  @ParameterizedTest
  @CsvSource({"apple, banana, 10, 10, 1", "banana, apple, 10, 10, -1", "apple, apple, 10, 10, 0"})
  void compareToWhenWeightsEqualComparesWords(String word1, String word2, int weight1,
      int weight2, int expected) {
    Candidate c1 = new Candidate(word1, weight1);
    Candidate c2 = new Candidate(word2, weight2);
    assertEquals(expected, c1.compareTo(c2));
  }

  @Test
  void compareToWithNullThrowsNullPointerException() {
    Candidate candidate = new Candidate("test", 5);
    assertThrows(NullPointerException.class, () -> candidate.compareTo(null));
  }

  @Test
  void constructorWithNegativeWeightIsAllowed() {
    assertDoesNotThrow(() -> new Candidate("test", -1));
  }

  @Test
  void recordAccessorsReturnCorrectValues() {
    Candidate candidate = new Candidate("word", 42);
    assertEquals("word", candidate.word());
    assertEquals(42, candidate.weight());
  }
}
