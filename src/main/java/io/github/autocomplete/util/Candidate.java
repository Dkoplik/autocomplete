package io.github.autocomplete.util;

/**
 * Кандидат автодополнения.
 * 
 * @param word Слово
 * 
 * @param weight Вес
 */
public record Candidate(String word, double weight) implements Comparable<Candidate> {
  /**
   * Сравнивает кандидатов по весу, если вес одинаковый, то по слову
   * 
   * @param other Другой кандидат
   * 
   * @return Результат сравнения
   */
  @Override
  public int compareTo(Candidate other) {
    int weightCompare = Double.compare(this.weight, other.weight);
    return weightCompare != 0 ? weightCompare : other.word.compareTo(this.word);
  }
}
