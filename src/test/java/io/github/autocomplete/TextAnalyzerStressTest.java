package io.github.autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.github.autocomplete.tokenizer.SimpleTokenizer;
import io.github.autocomplete.tokenizer.TokenizerConfig;
import io.github.autocomplete.util.WordFrequency;

/**
 * Стресс-тесты для TextAnalyzer
 */
@Tag("stress")
@Tag("performance")
class TextAnalyzerStressTest {

  private static final int LARGE_DATA_SIZE = 1_000_000;
  // Видимо, требует слишком много ОЗУ для github actions
  // private static final int VERY_LARGE_DATA_SIZE = 10_000_000;
  private static final int UNIQUE_WORDS = 50_000;
  private static final int TOP_WORDS_LIMIT = 1000;

  private TextAnalyzer analyzer;
  private Random random;
  private List<String> dictionary;

  @BeforeEach
  void setUp() {
    TokenizerConfig config = new TokenizerConfig("\\s+", c -> true, false);
    SimpleTokenizer tokenizer = new SimpleTokenizer();
    tokenizer.setConfig(config);

    analyzer = new TextAnalyzer(tokenizer);
    random = new Random();

    // словарь уникальных слов
    dictionary = IntStream.range(0, UNIQUE_WORDS).mapToObj(i -> generateRandomWord(5, 15))
        .collect(Collectors.toList());
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
  @Timeout(value = 1, unit = TimeUnit.MINUTES)
  void processLargeText_Performance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);

    long startTime = System.nanoTime();
    analyzer.addText(largeText);
    long duration = System.nanoTime() - startTime;

    System.out.printf("Processing %,d words took: %d ms%n", LARGE_DATA_SIZE,
        TimeUnit.NANOSECONDS.toMillis(duration));

    Map<String, Integer> allWords = analyzer.getAllWords();
    assertTrue(allWords.size() > 0);
    assertTrue(allWords.size() <= UNIQUE_WORDS);
  }

  // Походу требует слишком много ОЗУ для github actions
  // @Test
  // @Timeout(value = 30, unit = TimeUnit.SECONDS)
  // void processVeryLargeText_Performance() {
  // String veryLargeText = generateLargeText(VERY_LARGE_DATA_SIZE);

  // long startTime = System.nanoTime();
  // analyzer.processText(veryLargeText);
  // long duration = System.nanoTime() - startTime;

  // System.out.printf("Processing %,d words took: %d ms%n", VERY_LARGE_DATA_SIZE,
  // TimeUnit.NANOSECONDS.toMillis(duration));

  // int totalWords = analyzer.getAllWords().values().stream().mapToInt(Integer::intValue).sum();
  // assertEquals(VERY_LARGE_DATA_SIZE, totalWords);
  // }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getTopWords_LargeDataset_Performance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    analyzer.addText(largeText);

    long startTime = System.nanoTime();
    List<WordFrequency> topWords = analyzer.getTopWords(TOP_WORDS_LIMIT);
    long duration = System.nanoTime() - startTime;

    System.out.printf("Getting top %,d words from %,d total took: %d ms%n", TOP_WORDS_LIMIT,
        LARGE_DATA_SIZE, TimeUnit.NANOSECONDS.toMillis(duration));

    assertEquals(TOP_WORDS_LIMIT, topWords.size());
    assertNotNull(topWords);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getAllWords_LargeDataset_Performance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    analyzer.addText(largeText);

    long startTime = System.nanoTime();
    Map<String, Integer> allWords = analyzer.getAllWords();
    long duration = System.nanoTime() - startTime;

    System.out.printf("Getting all words (%,d unique) took: %d ms%n", allWords.size(),
        TimeUnit.NANOSECONDS.toMillis(duration));

    int totalCount = allWords.values().stream().mapToInt(Integer::intValue).sum();
    assertEquals(LARGE_DATA_SIZE, totalCount);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void multipleProcessTextCalls_Performance() {
    int chunks = 10;
    int chunkSize = LARGE_DATA_SIZE / chunks;

    long totalDuration = 0;

    for (int i = 0; i < chunks; i++) {
      String chunk = generateLargeText(chunkSize);

      long startTime = System.nanoTime();
      analyzer.addText(chunk);
      totalDuration += System.nanoTime() - startTime;
    }

    System.out.printf("Processing %,d words in %,d chunks took: %d ms%n", LARGE_DATA_SIZE, chunks,
        TimeUnit.NANOSECONDS.toMillis(totalDuration));

    int totalWords = analyzer.getAllWords().values().stream().mapToInt(Integer::intValue).sum();
    assertEquals(LARGE_DATA_SIZE, totalWords);
  }

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void getWordsByRegex_LargeDataset_Performance() {
    String largeText = generateLargeText(LARGE_DATA_SIZE);
    analyzer.addText(largeText);

    long startTime = System.nanoTime();
    Map<String, Integer> wordsStartingWithA = analyzer.getWordsByRegex("a.*");
    long duration = System.nanoTime() - startTime;

    System.out.printf("Regex search on %,d words took: %d ms%n", LARGE_DATA_SIZE,
        TimeUnit.NANOSECONDS.toMillis(duration));

    assertTrue(wordsStartingWithA.keySet().stream().allMatch(word -> word.startsWith("a")));
  }
}
