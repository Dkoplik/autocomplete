package io.github.autocomplete.util;

/**
 * Пара значений
 * 
 * @param <A> Тип первого элемента пары
 * 
 * @param <B> Тип второго элемента пары
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

  /**
   * Создает пару значений
   * 
   * @param first Первый элемент пары
   * 
   * @param second Второй элемент пары
   */
  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }
}
