package io.github.autocomplete.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevenshteinTest {
  @Test
  void distance_IdenticalStrings_ReturnsZero() {
    assertEquals(0, Levenshtein.distance("kitten", "kitten"));
    assertEquals(0, Levenshtein.distance("", ""));
  }

  @Test
  void distance_EmptyString_ReturnsLengthOfOther() {
    assertEquals(6, Levenshtein.distance("kitten", ""));
    assertEquals(6, Levenshtein.distance("", "kitten"));
  }

  @Test
  void distance_SingleEditCases() {
    assertEquals(1, Levenshtein.distance("kitten", "sitten"));
    assertEquals(1, Levenshtein.distance("kitten", "kitte"));
    assertEquals(1, Levenshtein.distance("kitten", "kittena"));
  }

  @Test
  void distance_MultipleEdits() {
    assertEquals(3, Levenshtein.distance("kitten", "sitting"));
    assertEquals(2, Levenshtein.distance("flaw", "lawn"));
    assertEquals(5, Levenshtein.distance("intention", "execution"));
  }

  @Test
  void distance_CaseSensitivity() {
    assertEquals(1, Levenshtein.distance("abc", "Abc"));
  }
}
