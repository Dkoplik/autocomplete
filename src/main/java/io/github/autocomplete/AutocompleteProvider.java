package io.github.autocomplete;

import io.github.autocomplete.cache.LRUCache;
import io.github.autocomplete.config.AutocompleteConfig;
import io.github.autocomplete.model.Candidate;
import io.github.autocomplete.model.WordFrequency;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Поставщик автодополнений с опциональным кешированием.
 */
public class AutocompleteProvider {
  private final TextAnalyzer textAnalyzer;
  private final LRUCache<String, List<Candidate>> cache;
  private final LRUCache<String, List<String>> similarPrefixCache;
  private AutocompleteConfig config;
  private static final int DEFAULT_CACHE_SIZE = 100;

  /**
   * Инициализация автодополнений на основе текстового анализатора.
   *
   * @param textAnalyzer Текстовый анализатор на основе которого определяются автодополнения
   *
   * @throws IllegalArgumentException Если textAnalyzer равен null
   */
  public AutocompleteProvider(TextAnalyzer textAnalyzer) {
    this(textAnalyzer, new AutocompleteConfig(), DEFAULT_CACHE_SIZE);
  }

  /**
   * Инициализация автодополнений на основе текстового анализатора.
   *
   * @param textAnalyzer Текстовый анализатор на основе которого определяются автодополнения
   *
   * @param cacheSize Размер LRU-кеша
   *
   * @throws IllegalArgumentException Если cacheSize меньше 0
   *
   * @throws IllegalArgumentException Если textAnalyzer равен null
   */
  public AutocompleteProvider(TextAnalyzer textAnalyzer, int cacheSize) {
    this(textAnalyzer, new AutocompleteConfig(), cacheSize);
  }

  /**
   * Инициализация автодополнений с конфигом.
   *
   * @param textAnalyzer Текстовый анализатор на основе которого определяются автодополнения
   *
   * @param config Конфигурация автодополнений
   *
   * @throws IllegalArgumentException Если config равен null
   *
   * @throws IllegalArgumentException Если textAnalyzer равен null
   */
  public AutocompleteProvider(TextAnalyzer textAnalyzer, AutocompleteConfig config) {
    this(textAnalyzer, config, DEFAULT_CACHE_SIZE);
  }

  /**
   * Инициализация автодополнений на основе текстового анализатора.
   *
   * @param textAnalyzer Текстовый анализатор на основе которого определяются автодополнения
   *
   * @param config Конфигурация автодополнений
   *
   * @param cacheSize Размер LRU-кеша
   *
   * @throws IllegalArgumentException Если cacheSize меньше 0
   *
   * @throws IllegalArgumentException Если config равен null
   *
   * @throws IllegalArgumentException Если textAnalyzer равен null
   */
  public AutocompleteProvider(TextAnalyzer textAnalyzer, AutocompleteConfig config, int cacheSize) {
    if (cacheSize < 0) {
      throw new IllegalArgumentException("Размер кеша не может быть отрицательным");
    }
    if (config == null) {
      throw new IllegalArgumentException("config cannot be null");
    }
    if (textAnalyzer == null) {
      throw new IllegalArgumentException("textAnalyzer cannot be null");
    }

    this.textAnalyzer = textAnalyzer;
    this.config = config;
    this.cache = cacheSize > 0 ? new LRUCache<>(cacheSize) : null;
    this.similarPrefixCache = cacheSize > 0 ? new LRUCache<>(cacheSize) : null;
  }

  /**
   * Получить конфигурацию автодополнения {@link AutocompleteConfig}.
   *
   * @return Конфигурация автодополнения
   */
  public AutocompleteConfig getConfig() {
    return config;
  }

  /**
   * Установить конфигурацию автодополнения {@link AutocompleteConfig}. Сбрасывает кеш.
   *
   * @param config Конфигурация автодополнения
   *
   * @throws IllegalArgumentException Если config равен null
   */
  public void setConfig(AutocompleteConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("config cannot be null");
    }

    this.config = config;
    if (cache != null) {
      cache.clear();
    }
    if (similarPrefixCache != null) {
      similarPrefixCache.clear();
    }
  }

  /**
   * Получить варианты автодополнения с учётом конфигурации (опечатки, веса и т.д.).
   *
   * @param prefix Префикс на основе которго происходит автодополнение
   *
   * @param limit максимальное количество вариантов автодополнения
   *
   * @return Список вариантов автодополнения
   *
   * @throws IllegalArgumentException Если prefix равен null или пустой строке ИЛИ limit меньше или
   *         равен 0
   */
  public List<Candidate> getAutocomplete(String prefix, int limit) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("Prefix cannot be null or empty");
    }
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be positive number");
    }

    String key = isCaseInsensitive() ? prefix.toLowerCase() : prefix;
    String lookupPrefix = isCaseInsensitive() ? prefix.toLowerCase() : prefix;

    List<Candidate> cached = getCachedCandidates(key, limit);
    if (cached != null) {
      return cached;
    }
    List<Candidate> candidates =
        (config.tolerance() <= 0) ? getExactPrefixCandidates(lookupPrefix, limit)
            : getTypoToleranceCandidates(lookupPrefix, limit, key);
    putCachedCandidates(key, candidates);
    return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
  }

  /**
   * Получить кэшированные кандидаты, если они есть и подходят по размеру.
   *
   * @param key Ключ для поиска в кеше
   *
   * @param limit Максимальное количество кандидатов
   *
   * @return Список кандидатов
   */
  private List<Candidate> getCachedCandidates(String key, int limit) {
    if (cache != null) {
      List<Candidate> cached = cache.get(key);
      if (cached != null && cached.size() >= limit) {
        return cached.subList(0, limit);
      }
    }
    return null;
  }

  /**
   * Положить кандидатов в кэш.
   *
   * @param key Ключ для поиска в кеше
   *
   * @param candidates Список кандидатов
   */
  private void putCachedCandidates(String key, List<Candidate> candidates) {
    if (cache != null) {
      cache.put(key, candidates);
    }
  }

  /**
   * Получить кандидатов для точного совпадения префикса.
   *
   * @param prefix Префикс на основе которго происходит автодополнение
   *
   * @param limit Максимальное количество вариантов автодополнения
   *
   * @return Список вариантов автодополнения
   */
  private List<Candidate> getExactPrefixCandidates(String prefix, int limit) {
    List<WordFrequency> result = textAnalyzer.getTrie().findCompletions(prefix, limit);
    return result.stream()
        .map(wf -> new Candidate(wf.word(), wf.frequency() * config.originalWeight()))
        .collect(Collectors.toList());
  }

  /**
   * Получить кандидатов с учётом опечаток (похожих префиксов).
   *
   * @param prefix Префикс на основе которго происходит автодополнение
   *
   * @param limit Максимальное количество вариантов автодополнения
   *
   * @param key Ключ для поиска в кеше
   *
   * @return Список вариантов автодополнения
   */
  private List<Candidate> getTypoToleranceCandidates(String prefix, int limit, String key) {
    List<String> similarPrefixes = getCachedSimilarPrefixes(key, prefix);
    Set<String> seen = new HashSet<>();
    List<Candidate> all = new ArrayList<>();
    List<WordFrequency> orig = textAnalyzer.getTrie().findCompletions(prefix, limit);
    for (WordFrequency wf : orig) {
      all.add(new Candidate(wf.word(), wf.frequency() * config.originalWeight()));
      seen.add(wf.word());
    }
    for (String simPrefix : similarPrefixes) {
      if (simPrefix.equals(prefix)) {
        continue;
      }
      List<WordFrequency> sim = textAnalyzer.getTrie().findCompletions(simPrefix, limit);
      for (WordFrequency wf : sim) {
        if (seen.add(wf.word())) {
          all.add(new Candidate(wf.word(), wf.frequency() * config.similarWeight()));
        }
      }
    }
    return all.stream().sorted((a, b) -> {
      int cmp = Double.compare(b.weight(), a.weight());
      return cmp != 0 ? cmp : a.word().compareTo(b.word());
    }).collect(Collectors.toList());
  }

  /**
   * Получить кэшированные похожие префиксы или вычислить и закэшировать их.
   *
   * @param key Ключ для поиска в кеше
   *
   * @param prefix Префикс на основе которго происходит автодополнение
   *
   * @return Список похожих префиксов
   */
  private List<String> getCachedSimilarPrefixes(String key, String prefix) {
    if (similarPrefixCache != null) {
      List<String> cachedSim = similarPrefixCache.get(key);
      if (cachedSim != null) {
        return cachedSim;
      } else {
        List<String> similarPrefixes = textAnalyzer.getTrie().findSimilarPrefixes(prefix,
            config.tolerance(), config.toleranceThreshold(), config.distanceFunction());
        similarPrefixCache.put(key, similarPrefixes);
        return similarPrefixes;
      }
    } else {
      return textAnalyzer.getTrie().findSimilarPrefixes(prefix, config.tolerance(),
          config.toleranceThreshold(), config.distanceFunction());
    }
  }

  /**
   * Добавить текст для обновления данных. Сбрасывает кеш.
   *
   * @param text Текст для обновления данных
   *
   * @throws IllegalArgumentException Если text равен null
   */
  public void addText(String text) {
    if (text == null) {
      throw new IllegalArgumentException("text cannot be null");
    }

    textAnalyzer.addText(text);

    if (cache != null) {
      cache.clear();
    }
    if (similarPrefixCache != null) {
      similarPrefixCache.clear();
    }
  }

  private boolean isCaseInsensitive() {
    return textAnalyzer.getTokenizer().getConfig().toLowerCase();
  }
}
