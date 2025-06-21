package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LevenshteinTest {

  @Test
  void distanceIdenticalStringsReturnsZero() {
    assertEquals(0, Levenshtein.distance("kitten", "kitten"));
    assertEquals(0, Levenshtein.distance("", ""));
  }

  @Test
  void distanceEmptyStringReturnsLengthOfOther() {
    assertEquals(6, Levenshtein.distance("kitten", ""));
    assertEquals(6, Levenshtein.distance("", "kitten"));
  }

  @Test
  void distanceSingleEditCases() {
    assertEquals(1, Levenshtein.distance("kitten", "sitten"));
    assertEquals(1, Levenshtein.distance("kitten", "kitte"));
    assertEquals(1, Levenshtein.distance("kitten", "kittena"));
  }

  @Test
  void distanceMultipleEdits() {
    assertEquals(3, Levenshtein.distance("kitten", "sitting"));
    assertEquals(2, Levenshtein.distance("flaw", "lawn"));
    assertEquals(5, Levenshtein.distance("intention", "execution"));
  }

  @Test
  void distanceCaseSensitivity() {
    assertEquals(1, Levenshtein.distance("abc", "Abc"));
  }
}
