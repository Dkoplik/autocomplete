package io.github.autocomplete.util;

/**
 * Record для хранения слова и его частоты (frequency).
 */
public record WordFrequency(String word, int frequency) implements Comparable<WordFrequency> {
  public WordFrequency {
    if (frequency < 0) {
      throw new IllegalArgumentException("Frequency cannot be negative: " + frequency);
    }
  }

  @Override
  public int compareTo(WordFrequency other) {
    int weightCompare = Integer.compare(this.frequency, other.frequency);
    return weightCompare != 0 ? weightCompare : other.word.compareTo(this.word);
  }
}
