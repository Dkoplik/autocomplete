package io.github.autocomplete.util;

/**
 * Кандидат автодополнения
 */
public record Candidate(String word, double weight) implements Comparable<Candidate> {
  @Override
  public int compareTo(Candidate other) {
    int weightCompare = Double.compare(this.weight, other.weight);
    return weightCompare != 0 ? weightCompare : other.word.compareTo(this.word);
  }
}
