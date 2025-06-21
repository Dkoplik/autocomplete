package io.github.autocomplete;

import java.util.function.BiFunction;
import io.github.autocomplete.util.Levenshtein;

/**
 * Конфигурация автодополнения.
 * 
 * @param distanceFunction Функция расстояния между двумя строками
 * @param toleranceThreshold Пороговое значение длины строки, для которой применяется толерантность
 * @param tolerance Максимальное расстояние между строками
 * @param similarWeight Вес для похожих префиксов
 * @param originalWeight Вес для оригинальных префиксов
 */
public record AutocompleteConfig(BiFunction<String, String, Integer> distanceFunction,
    int toleranceThreshold, int tolerance, double similarWeight, double originalWeight) {
  public AutocompleteConfig {
    if (distanceFunction == null) {
      throw new IllegalArgumentException("distanceFunction cannot be null");
    }
    if (toleranceThreshold < 0) {
      throw new IllegalArgumentException("toleranceThreshold cannot be negative");
    }
    if (tolerance < 0) {
      throw new IllegalArgumentException("tolerance cannot be negative");
    }
  }

  /**
   * Конструктор по умолчанию.
   */
  public AutocompleteConfig() {
    this(Levenshtein::distance, 0, 0, 0.5, 1.0);
  }
}
