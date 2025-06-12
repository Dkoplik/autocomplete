package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CandidateTest {

    @Test
    void compareTo_WhenFrequenciesDiffer_ReturnsCorrectComparison() {
        Candidate highFreq = new Candidate("test", 10);
        Candidate lowFreq = new Candidate("test", 5);
        assertTrue(highFreq.compareTo(lowFreq) > 0);
        assertTrue(lowFreq.compareTo(highFreq) < 0);
    }

    @ParameterizedTest
    @CsvSource({"apple, banana, 10, 10, 1", "banana, apple, 10, 10, -1", "apple, apple, 10, 10, 0"})
    void compareTo_WhenFrequenciesEqual_ComparesWords(String word1, String word2, int freq1,
            int freq2, int expected) {
        Candidate c1 = new Candidate(word1, freq1);
        Candidate c2 = new Candidate(word2, freq2);
        assertEquals(expected, c1.compareTo(c2));
    }

    @Test
    void compareTo_WithNull_ThrowsNullPointerException() {
        Candidate candidate = new Candidate("test", 5);
        assertThrows(NullPointerException.class, () -> candidate.compareTo(null));
    }

    @Test
    void constructor_WithNegativeFrequency_IsAllowed() {
        assertDoesNotThrow(() -> new Candidate("test", -1));
    }
}
