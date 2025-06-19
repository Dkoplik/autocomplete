package io.github.autocomplete;

import java.util.List;
import io.github.autocomplete.cache.LRUCache;
import io.github.autocomplete.util.Candidate;

/**
 * Поставщик автодополнений с кешированием
 */
public class AutocompleteProvider {
  private final TextAnalyzer textAnalyzer;
  private final LRUCache<String, List<Candidate>> cache;
  private static final int DEFAULT_CACHE_SIZE = 100;

  /**
   * Инициализация автодополнений на основе текстового анализатора. Использует стаднартный размер
   * кеша
   * 
   * @param textAnalyzer Текстовый анализатор на основе которого определяются автодополнения
   */
  public AutocompleteProvider(TextAnalyzer textAnalyzer) {
    this(textAnalyzer, DEFAULT_CACHE_SIZE);
  }

  /**
   * Инициализация автодополнений на основе текстового анализатора
   * 
   * @param textAnalyzer Текстовый анализатор на основе которого определяются автодополнения
   * @param cacheSize Размер LRU-кеша
   */
  public AutocompleteProvider(TextAnalyzer textAnalyzer, int cacheSize) {
    if (cacheSize < 0) {
      throw new IllegalArgumentException("Размер кеша не может быть отрицательным");
    }

    this.textAnalyzer = textAnalyzer;
    this.cache = cacheSize > 0 ? new LRUCache<>(cacheSize) : null;
  }

  /**
   * Получить варианты автодополнения
   * 
   * @param prefix Префикс на основе которго происходит автодополнение
   * @param limit максимальное количество вариантов автодополнения
   */
  public List<Candidate> getAutocomplete(String prefix, int limit) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("Prefix cannot be null or empty");
    }

    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be positive number");
    }

    String key = prefix.toLowerCase();
    if (cache != null) {
      List<Candidate> cached = cache.get(key);
      if (cached != null)
        return cached;
    }

    List<Candidate> result = textAnalyzer.getCompletions(prefix, limit);

    if (cache != null) {
      cache.put(key, result);
    }

    return result;
  }

  /**
   * Добавить текст для обновления данных
   */
  public void processText(String text) {
    textAnalyzer.processText(text);

    if (cache != null) {
      cache.clear();
    }
  }
}
