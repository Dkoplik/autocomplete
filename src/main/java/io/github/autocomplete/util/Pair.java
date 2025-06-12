package io.github.autocomplete.util;

/**
 * Пара значений
 */
public class Pair<A, B> {
  /**
   * Первый элемент пары
   */
  public final A first;
  /**
   * Второй элемент пары
   */
  public final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }
}
