package io.github.autocomplete.util;

/**
 * Record для хранения слова и его частоты (frequency).
 */
public record WordFrequency(String word, int frequency) implements Comparable<WordFrequency> {
  /**
   * Создает запись для слова и его частоты
   * 
   * @param word Слово
   * 
   * @param frequency Частота
   * 
   * @throws IllegalArgumentException Если частота меньше 0
   */
  public WordFrequency {
    if (frequency < 0) {
      throw new IllegalArgumentException("Frequency cannot be negative: " + frequency);
    }
  }

  /**
   * Сравнивает слова по частоте, если частота одинаковая, то по слову
   * 
   * @param other Другое слово
   * 
   * @return Результат сравнения
   */
  @Override
  public int compareTo(WordFrequency other) {
    int weightCompare = Integer.compare(this.frequency, other.frequency);
    return weightCompare != 0 ? weightCompare : other.word.compareTo(this.word);
  }
}
