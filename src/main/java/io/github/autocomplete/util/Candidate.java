package io.github.autocomplete.util;

/**
 * Кандидат автодополнения
 */
public record Candidate(String word, int frequency) implements Comparable<Candidate> {
  @Override
  public int compareTo(Candidate other) {
    int freqCompare = Integer.compare(this.frequency, other.frequency);
    return freqCompare != 0 ? freqCompare : other.word.compareTo(this.word);
  }
}
