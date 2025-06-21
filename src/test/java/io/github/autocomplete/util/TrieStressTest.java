package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import io.github.autocomplete.model.WordFrequency;
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

/**
 * Стресс-тесты для реализации префиксного дерева (Trie)
 */
@Tag("stress")
@Tag("performance")
class TrieStressTest {

  private static final int LARGE_DATA_SIZE = 1_000_000;
  private static final int VERY_LARGE_DATA_SIZE = 10_000_000;
  private static final int UNIQUE_WORDS = 100_000;
  private static final int COMPLETION_LIMIT = 100;
  private static final int TOP_WORDS_LIMIT = 1000;

  private Trie trie;
  private Random random;
  private List<String> dictionary;
  private List<String> prefixes;

  @BeforeEach
  void setUp() {
    trie = new Trie();
    random = new Random();

    dictionary = IntStream.range(0, UNIQUE_WORDS).mapToObj(i -> generateRandomWord(3, 10))
        .collect(Collectors.toList());

    prefixes = IntStream.range(0, 1000).mapToObj(i -> {
      String word = dictionary.get(random.nextInt(dictionary.size()));
      return word.substring(0, Math.min(2, word.length()));
    }).distinct().collect(Collectors.toList());
  }

  private String generateRandomWord(int minLength, int maxLength) {
    int length = minLength + random.nextInt(maxLength - minLength + 1);
    return random.ints(length, 'a', 'z' + 1)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  @Test
  @Timeout(value = 20, unit = TimeUnit.SECONDS)
  void insertLargeDatasetPerformance() {
    long startTime = System.nanoTime();

    for (int i = 0; i < LARGE_DATA_SIZE; i++) {
      String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
      trie.insert(word);
    }

    long duration = System.nanoTime() - startTime;
    System.out.printf("Inserted %,d words in %d ms%n", LARGE_DATA_SIZE,
        TimeUnit.NANOSECONDS.toMillis(duration));

    int uniqueWords = trie.getAllWords().size();
    System.out.printf("Unique words after insert: %,d%n", uniqueWords);
    assertTrue(uniqueWords > 0 && uniqueWords <= UNIQUE_WORDS);
  }

  @Test
  @Timeout(value = 40, unit = TimeUnit.SECONDS)
  void insertVeryLargeDatasetPerformance() {
    long startTime = System.nanoTime();

    for (int i = 0; i < VERY_LARGE_DATA_SIZE; i++) {
      String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
      trie.insert(word);
    }

    long duration = System.nanoTime() - startTime;
    System.out.printf("Inserted %,d words in %d ms%n", VERY_LARGE_DATA_SIZE,
        TimeUnit.NANOSECONDS.toMillis(duration));

    int totalFrequency = trie.getAllWords().values().stream().mapToInt(Integer::intValue).sum();
    assertEquals(VERY_LARGE_DATA_SIZE, totalFrequency);
  }

  @Test
  @Timeout(value = 40, unit = TimeUnit.SECONDS)
  void findCompletionsPerformance() {
    for (int i = 0; i < LARGE_DATA_SIZE; i++) {
      String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
      trie.insert(word);
    }

    long totalDuration = 0;
    int queryCount = 100_000;

    for (int i = 0; i < queryCount; i++) {
      String prefix = prefixes.get(random.nextInt(prefixes.size()));

      long startTime = System.nanoTime();
      List<WordFrequency> completions = trie.findCompletions(prefix, COMPLETION_LIMIT);
      totalDuration += System.nanoTime() - startTime;

      assertNotNull(completions);
      assertTrue(completions.size() <= COMPLETION_LIMIT);
    }

    System.out.printf("Executed %,d findCompletions queries in %d ms (avg: %.2f ms/query)%n",
        queryCount, TimeUnit.NANOSECONDS.toMillis(totalDuration),
        (double) TimeUnit.NANOSECONDS.toMillis(totalDuration) / queryCount);
  }

  @Test
  @Timeout(value = 20, unit = TimeUnit.SECONDS)
  void getTopFrequentWordsPerformance() {
    for (int i = 0; i < LARGE_DATA_SIZE; i++) {
      String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
      trie.insert(word);
    }

    long startTime = System.nanoTime();
    List<WordFrequency> topWords = trie.getTopFrequentWords(TOP_WORDS_LIMIT);
    long duration = System.nanoTime() - startTime;

    System.out.printf("getTopFrequentWords(%,d) took: %d ms%n", TOP_WORDS_LIMIT,
        TimeUnit.NANOSECONDS.toMillis(duration));

    assertEquals(TOP_WORDS_LIMIT, topWords.size());
    assertSortedByFrequency(topWords);
  }

  @Test
  @Timeout(value = 20, unit = TimeUnit.SECONDS)
  void getAllWordsPerformance() {
    for (int i = 0; i < LARGE_DATA_SIZE; i++) {
      String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
      trie.insert(word);
    }

    long startTime = System.nanoTime();
    Map<String, Integer> allWords = trie.getAllWords();
    long duration = System.nanoTime() - startTime;

    System.out.printf("getAllWords() with %,d unique words took: %d ms%n", allWords.size(),
        TimeUnit.NANOSECONDS.toMillis(duration));

    int totalFrequency = allWords.values().stream().mapToInt(Integer::intValue).sum();
    assertEquals(LARGE_DATA_SIZE, totalFrequency);
  }

  @Test
  @Timeout(value = 1, unit = TimeUnit.MINUTES)
  void mixedOperationsPerformance() {
    int operationsCount = LARGE_DATA_SIZE;
    double insertChance = 0.7;
    double findCompletionsChance = 0.2999;

    long insertTime = 0;
    int insertCount = 0;

    long completionTime = 0;
    int completionsCount = 0;

    long topWordsTime = 0;
    int topWordsCount = 0;

    for (int i = 0; i < operationsCount; i++) {
      double opType = random.nextDouble();

      if (opType < insertChance) {
        String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
        long start = System.nanoTime();
        trie.insert(word);
        insertTime += System.nanoTime() - start;
        insertCount++;
      } else if (opType < insertChance + findCompletionsChance) {
        String prefix = prefixes.get(random.nextInt(prefixes.size()));
        long start = System.nanoTime();
        trie.findCompletions(prefix, COMPLETION_LIMIT);
        completionTime += System.nanoTime() - start;
        completionsCount++;
      } else {
        long start = System.nanoTime();
        trie.getTopFrequentWords(TOP_WORDS_LIMIT);
        topWordsTime += System.nanoTime() - start;
        topWordsCount++;
      }
    }

    System.out.println("Mixed operations performance:");
    System.out.printf("  Insert operations: %,d in %d ms%n", insertCount,
        TimeUnit.NANOSECONDS.toMillis(insertTime));
    System.out.printf("  Completion queries: %,d in %d ms%n", completionsCount,
        TimeUnit.NANOSECONDS.toMillis(completionTime));
    System.out.printf("  Top words queries: %,d in %d ms%n", topWordsCount,
        TimeUnit.NANOSECONDS.toMillis(topWordsTime));
    System.out.printf("  Total time: %d ms%n",
        TimeUnit.NANOSECONDS.toMillis(insertTime + completionTime + topWordsTime));
  }

  @Test
  @Timeout(value = 20, unit = TimeUnit.SECONDS)
  void removeWordsPerformance() {
    for (int i = 0; i < LARGE_DATA_SIZE; i++) {
      String word = dictionary.get(random.nextInt(UNIQUE_WORDS));
      trie.insert(word);
    }

    List<String> wordsToRemove =
        IntStream.range(0, 100_000).mapToObj(i -> dictionary.get(random.nextInt(UNIQUE_WORDS)))
            .distinct().collect(Collectors.toList());

    long startTime = System.nanoTime();
    for (String word : wordsToRemove) {
      trie.remove(word);
    }
    long duration = System.nanoTime() - startTime;

    System.out.printf("Removed %,d words in %d ms%n", wordsToRemove.size(),
        TimeUnit.NANOSECONDS.toMillis(duration));

    for (String word : wordsToRemove) {
      assertEquals(0, trie.getFrequency(word));
    }
  }

  private void assertSortedByFrequency(List<WordFrequency> candidates) {
    for (int i = 0; i < candidates.size() - 1; i++) {
      WordFrequency current = candidates.get(i);
      WordFrequency next = candidates.get(i + 1);

      assertTrue(current.frequency() >= next.frequency(),
          "Candidates not sorted by frequency: " + current + " before " + next);

      if (current.frequency() == next.frequency()) {
        assertTrue(current.word().compareTo(next.word()) <= 0,
            "Words with same frequency not sorted alphabetically: " + current.word() + " and "
                + next.word());
      }
    }
  }
}
