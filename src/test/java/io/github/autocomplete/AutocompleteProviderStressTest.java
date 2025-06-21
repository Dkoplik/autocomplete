package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import io.github.autocomplete.config.AutocompleteConfig;
import io.github.autocomplete.config.TokenizerConfig;
import io.github.autocomplete.model.Candidate;
import io.github.autocomplete.tokenizer.SimpleTokenizer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Стресс-тесты для AutocompleteProvider
 */
@Tag("stress")
@Tag("performance")
class AutocompleteProviderStressTest {

  private static final int LARGE_DATA_SIZE = 1_000_000;
  private static final int VERY_LARGE_DATA_SIZE = 5_000_000;
  private static final int UNIQUE_WORDS = 50_000;
  private static final int QUERY_COUNT = 10_000;
  private static final int TOP_RESULTS_LIMIT = 100;

  private TextAnalyzer textAnalyzer;
  private AutocompleteProvider provider;
  private Random random;
  private List<String> dictionary;
  private List<String> popularPrefixes;

  @BeforeEach
  void setUp() {
    TokenizerConfig config = new TokenizerConfig("\\s+", c -> true, false);
    SimpleTokenizer tokenizer = new SimpleTokenizer();
    tokenizer.setConfig(config);

    textAnalyzer = new TextAnalyzer(tokenizer);
    random = new Random();

    dictionary = IntStream.range(0, UNIQUE_WORDS).mapToObj(i -> generateRandomWord(3, 10))
        .collect(Collectors.toList());

    popularPrefixes = IntStream.range(0, 100).mapToObj(i -> {
      String word = dictionary.get(random.nextInt(dictionary.size()));
      return word.substring(0, Math.min(3, word.length()));
    }).distinct().collect(Collectors.toList());
  }

  /**
   * Генерация случайного слова (набора символов) длиной в указанных пределах.
   */
  private String generateRandomWord(int minLength, int maxLength) {
    int length = minLength + random.nextInt(maxLength - minLength + 1);
    return random.ints(length, 'a', 'z' + 1)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  /**
   * Сгенерировать текст с указанным количеством слов.
   */
  private String generateLargeText(int wordCount) {
    StringBuilder sb = new StringBuilder(wordCount * 10);
    for (int i = 0; i < wordCount; i++) {
      if (i > 0)
        sb.append(' ');
      sb.append(dictionary.get(random.nextInt(UNIQUE_WORDS)));
    }
    return sb.toString();
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void processLargeTextPerformance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    provider = new AutocompleteProvider(textAnalyzer);

    long startTime = System.nanoTime();
    provider.addText(largeText);
    long duration = System.nanoTime() - startTime;

    System.out.printf("Processing %,d words took: %d ms%n", LARGE_DATA_SIZE,
        TimeUnit.NANOSECONDS.toMillis(duration));
  }

  @Test
  @Timeout(value = 30, unit = TimeUnit.SECONDS)
  void processVeryLargeTextPerformance() {
    String veryLargeText = generateLargeText(VERY_LARGE_DATA_SIZE);
    provider = new AutocompleteProvider(textAnalyzer);

    long startTime = System.nanoTime();
    provider.addText(veryLargeText);
    long duration = System.nanoTime() - startTime;

    System.out.printf("Processing %,d words took: %d ms%n", VERY_LARGE_DATA_SIZE,
        TimeUnit.NANOSECONDS.toMillis(duration));
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getAutocompleteWithoutCachePerformance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    provider = new AutocompleteProvider(textAnalyzer, 0); // Без кеша

    provider.addText(largeText);

    long totalDuration = 0;

    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));

      long startTime = System.nanoTime();
      List<Candidate> results = provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
      totalDuration += System.nanoTime() - startTime;

      assertNotNull(results);
      assertTrue(results.size() <= TOP_RESULTS_LIMIT);
    }

    System.out.printf("%,d queries without cache took: %d ms (avg: %.2f ms/query)%n", QUERY_COUNT,
        TimeUnit.NANOSECONDS.toMillis(totalDuration),
        (double) TimeUnit.NANOSECONDS.toMillis(totalDuration) / QUERY_COUNT);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getAutocompleteWithCachePerformance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    provider = new AutocompleteProvider(textAnalyzer, 100); // С кешем

    provider.addText(largeText);

    long firstPassDuration = 0;
    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));

      long startTime = System.nanoTime();
      provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
      firstPassDuration += System.nanoTime() - startTime;
    }

    long secondPassDuration = 0;
    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));

      long startTime = System.nanoTime();
      provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
      secondPassDuration += System.nanoTime() - startTime;
    }

    System.out.printf("Cache warmup (%,d queries): %d ms%n", QUERY_COUNT,
        TimeUnit.NANOSECONDS.toMillis(firstPassDuration));
    System.out.printf("Cached queries (%,d queries): %d ms%n", QUERY_COUNT,
        TimeUnit.NANOSECONDS.toMillis(secondPassDuration));
    System.out.printf("Cache speedup: %.2fx%n", (double) firstPassDuration / secondPassDuration);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void mixedWorkloadPerformance() {
    provider = new AutocompleteProvider(textAnalyzer, 100); // С кешем

    int chunks = 10;
    int chunkSize = LARGE_DATA_SIZE / chunks;
    int queriesPerChunk = QUERY_COUNT / chunks;

    long totalProcessTime = 0;
    long totalQueryTime = 0;

    for (int i = 0; i < chunks; i++) {
      String chunk = generateLargeText(chunkSize);
      long processStart = System.nanoTime();
      provider.addText(chunk);
      totalProcessTime += System.nanoTime() - processStart;

      for (int j = 0; j < queriesPerChunk; j++) {
        String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));

        long queryStart = System.nanoTime();
        provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
        totalQueryTime += System.nanoTime() - queryStart;
      }
    }

    System.out.printf("Mixed workload (%,d words, %,d queries) took:%n", LARGE_DATA_SIZE,
        QUERY_COUNT);
    System.out.printf("  Processing: %d ms%n", TimeUnit.NANOSECONDS.toMillis(totalProcessTime));
    System.out.printf("  Queries: %d ms%n", TimeUnit.NANOSECONDS.toMillis(totalQueryTime));
    System.out.printf("  Total: %d ms%n",
        TimeUnit.NANOSECONDS.toMillis(totalProcessTime + totalQueryTime));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 10, 100, 1000})
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void cacheSizeImpactPerformance(int cacheSize) {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    provider = new AutocompleteProvider(textAnalyzer, cacheSize);
    provider.addText(largeText);

    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));
      provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
    }

    long totalDuration = 0;
    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));

      long startTime = System.nanoTime();
      provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
      totalDuration += System.nanoTime() - startTime;
    }

    System.out.printf("Cache size %,d: %,d queries took %d ms (avg: %.2f ms/query)%n", cacheSize,
        QUERY_COUNT, TimeUnit.NANOSECONDS.toMillis(totalDuration),
        (double) TimeUnit.NANOSECONDS.toMillis(totalDuration) / QUERY_COUNT);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void cacheEfficiencyWithRepeatedQueries() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    provider = new AutocompleteProvider(textAnalyzer, 100);
    provider.addText(largeText);

    List<String> repeatedPrefixes = popularPrefixes.subList(0, 10);

    long firstPassDuration = 0;
    long secondPassDuration = 0;

    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = repeatedPrefixes.get(random.nextInt(repeatedPrefixes.size()));

      long startTime = System.nanoTime();
      provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
      firstPassDuration += System.nanoTime() - startTime;
    }

    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = repeatedPrefixes.get(random.nextInt(repeatedPrefixes.size()));

      long startTime = System.nanoTime();
      provider.getAutocomplete(prefix, TOP_RESULTS_LIMIT);
      secondPassDuration += System.nanoTime() - startTime;
    }

    System.out.printf("Repeated queries cache efficiency:%n");
    System.out.printf("  First pass: %d ms%n", TimeUnit.NANOSECONDS.toMillis(firstPassDuration));
    System.out.printf("  Second pass: %d ms%n", TimeUnit.NANOSECONDS.toMillis(secondPassDuration));
    System.out.printf("  Speedup: %.2fx%n", (double) firstPassDuration / secondPassDuration);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getAutocompleteTypoTolerancePerformance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    AutocompleteConfig config =
        new AutocompleteConfig(io.github.autocomplete.util.Levenshtein::distance, 3, 1, 0.5, 1.0);
    provider = new AutocompleteProvider(textAnalyzer, config, 100);
    provider.addText(largeText);
    long totalDuration = 0;
    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));
      String typoPrefix = prefix.substring(0, prefix.length() - 1) + "x";
      long startTime = System.nanoTime();
      List<Candidate> results = provider.getAutocomplete(typoPrefix, TOP_RESULTS_LIMIT);
      totalDuration += System.nanoTime() - startTime;
      assertNotNull(results);
      assertTrue(results.size() <= TOP_RESULTS_LIMIT);
    }
    System.out.printf("Typo-tolerant %,d queries took: %d ms (avg: %.2f ms/query)%n", QUERY_COUNT,
        TimeUnit.NANOSECONDS.toMillis(totalDuration),
        (double) TimeUnit.NANOSECONDS.toMillis(totalDuration) / QUERY_COUNT);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getAutocompleteTypoToleranceCacheStress() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    AutocompleteConfig config =
        new AutocompleteConfig(io.github.autocomplete.util.Levenshtein::distance, 3, 1, 0.5, 1.0);
    provider = new AutocompleteProvider(textAnalyzer, config, 100);
    provider.addText(largeText);
    for (int i = 0; i < QUERY_COUNT; i++) {
      String prefix = popularPrefixes.get(random.nextInt(popularPrefixes.size()));
      String typoPrefix = prefix.substring(0, prefix.length() - 1) + "x";
      provider.getAutocomplete(typoPrefix, TOP_RESULTS_LIMIT);
    }
  }
}
