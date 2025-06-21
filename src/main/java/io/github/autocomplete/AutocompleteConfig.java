package io.github.autocomplete;

import io.github.autocomplete.util.Levenshtein;
import java.util.function.BiFunction;

/**
 * Конфигурация автодополнения.
 * 
 * @param distanceFunction Функция расстояния между двумя строками
 * 
 * @param toleranceThreshold Пороговое значение длины строки, для которой применяется толерантность
 * 
 * @param tolerance Максимальное расстояние между строками
 * 
 * @param similarWeight Вес для похожих префиксов
 * 
 * @param originalWeight Вес для оригинальных префиксов
 */
public record AutocompleteConfig(BiFunction<String, String, Integer> distanceFunction,
    int toleranceThreshold, int tolerance, double similarWeight, double originalWeight) {
  /**
   * Стандартный конструктор.
   * 
   * @throws IllegalArgumentException Если distanceFunction равен null ИЛИ toleranceThreshold меньше
   *         0 ИЛИ tolerance меньше 0
   */
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
