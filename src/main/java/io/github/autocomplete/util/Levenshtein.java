package io.github.autocomplete.util;

/**
 * Класс для вычисления расстояния Левенштейна между двумя строками.
 */
public class Levenshtein {
  /**
   * Вычисляет расстояние Левенштейна между двумя строками.
   *
   * @param a Первая строка
   *
   * @param b Вторая строка
   *
   * @return Расстояние Левенштейна
   */
  public static int distance(String a, String b) {
    int m = a.length();
    int n = b.length();
    if (m == 0) {
      return n;
    }
    if (n == 0) {
      return m;
    }
    int[] prev = new int[n + 1];
    int[] curr = new int[n + 1];
    for (int j = 0; j <= n; j++) {
      prev[j] = j;
    }
    for (int i = 1; i <= m; i++) {
      curr[0] = i;
      for (int j = 1; j <= n; j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
      }
      int[] tmp = prev;
      prev = curr;
      curr = tmp;
    }
    return prev[n];
  }
}
