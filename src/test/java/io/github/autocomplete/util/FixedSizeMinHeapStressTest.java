package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import io.github.autocomplete.model.WordFrequency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Стресс-тесты для FixedSizeMinHeap
 */
@Tag("stress")
@Tag("performance")
class FixedSizeMinHeapStressTest {

  private static final int LARGE_DATA_SIZE = 1_000_000;
  private static final int TOP_N = 1000;
  private static final int VERY_LARGE_DATA_SIZE = 10_000_000;

  private List<WordFrequency> dataset;
  private Random random;

  @BeforeEach
  void setUp() {
    random = new Random();
    dataset = IntStream.range(0, LARGE_DATA_SIZE)
        .mapToObj(i -> new WordFrequency(generateRandomWord(5, 12), random.nextInt(10_000)))
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

  @Test
  @Timeout(value = 20, unit = TimeUnit.SECONDS)
  void addLargeDatasetPerformance() {
    FixedSizeMinHeap heap = new FixedSizeMinHeap(TOP_N);
    long startTime = System.nanoTime();
    for (WordFrequency wf : dataset) {
      heap.add(wf);
    }
    long duration = System.nanoTime() - startTime;
    System.out.printf("FixedSizeMinHeap: Added %,d elements (top %,d kept) in %d ms\n",
        LARGE_DATA_SIZE, TOP_N, TimeUnit.NANOSECONDS.toMillis(duration));
    List<WordFrequency> top = heap.toSortedList();
    assertEquals(TOP_N, top.size());
    // Проверка убывания
    for (int i = 1; i < top.size(); i++) {
      assertTrue(top.get(i - 1).frequency() >= top.get(i).frequency());
    }
  }

  @Test
  @Timeout(value = 20, unit = TimeUnit.SECONDS)
  void toSortedListPerformance() {
    FixedSizeMinHeap heap = new FixedSizeMinHeap(TOP_N);
    for (WordFrequency wf : dataset) {
      heap.add(wf);
    }
    long startTime = System.nanoTime();
    List<WordFrequency> top = heap.toSortedList();
    long duration = System.nanoTime() - startTime;
    System.out.printf("FixedSizeMinHeap: toSortedList() for %,d elements took %d ns\n", TOP_N,
        TimeUnit.NANOSECONDS.toNanos(duration));
    assertEquals(TOP_N, top.size());
  }

  @Test
  @Timeout(value = 30, unit = TimeUnit.SECONDS)
  void compareWithPriorityQueueEfficiency() {
    // FixedSizeMinHeap
    FixedSizeMinHeap heap = new FixedSizeMinHeap(TOP_N);
    long startHeap = System.nanoTime();
    for (WordFrequency wf : dataset) {
      heap.add(wf);
    }
    List<WordFrequency> heapTop = heap.toSortedList();
    long heapDuration = System.nanoTime() - startHeap;

    // PriorityQueue
    PriorityQueue<WordFrequency> pq = new PriorityQueue<>(TOP_N);
    long startPQ = System.nanoTime();
    for (WordFrequency wf : dataset) {
      if (pq.size() < TOP_N) {
        pq.add(wf);
      } else if (wf.compareTo(pq.peek()) > 0) {
        pq.poll();
        pq.add(wf);
      }
    }
    List<WordFrequency> pqTop = new ArrayList<>(pq);
    Collections.sort(pqTop, Collections.reverseOrder());
    long pqDuration = System.nanoTime() - startPQ;

    System.out.printf("FixedSizeMinHeap: %,d elements, top %,d in %d ms\n", LARGE_DATA_SIZE, TOP_N,
        TimeUnit.NANOSECONDS.toMillis(heapDuration));
    System.out.printf("PriorityQueue: %,d elements, top %,d in %d ms\n", LARGE_DATA_SIZE, TOP_N,
        TimeUnit.NANOSECONDS.toMillis(pqDuration));
    if (pqDuration > 0) {
      double speedup = (double) pqDuration / heapDuration;
      System.out.printf(
          "Custom FixedSizeMinHeap is %.2fx faster than PriorityQueue implementation\n", speedup);
    } else {
      System.out.println("PriorityQueue time is 0");
    }

    assertEquals(TOP_N, heapTop.size());
    assertEquals(TOP_N, pqTop.size());
    // Проверка совпадения топ-элементов по частоте (может отличаться порядок при равных)
    List<Integer> heapFreqs = heapTop.stream().map(WordFrequency::frequency)
        .sorted(Collections.reverseOrder()).collect(Collectors.toList());
    List<Integer> pqFreqs = pqTop.stream().map(WordFrequency::frequency)
        .sorted(Collections.reverseOrder()).collect(Collectors.toList());
    assertEquals(heapFreqs, pqFreqs);
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.MINUTES)
  void addVeryLargeDatasetPerformance() {
    List<WordFrequency> veryLargeDataset = IntStream.range(0, VERY_LARGE_DATA_SIZE)
        .mapToObj(i -> new WordFrequency(generateRandomWord(5, 12), random.nextInt(10_000)))
        .collect(Collectors.toList());
    FixedSizeMinHeap heap = new FixedSizeMinHeap(TOP_N);
    long startTime = System.nanoTime();
    for (WordFrequency wf : veryLargeDataset) {
      heap.add(wf);
    }
    long duration = System.nanoTime() - startTime;
    System.out.printf("FixedSizeMinHeap: Added %,d elements (top %,d kept) in %d ms\n",
        VERY_LARGE_DATA_SIZE, TOP_N, TimeUnit.NANOSECONDS.toMillis(duration));
    List<WordFrequency> top = heap.toSortedList();
    assertEquals(TOP_N, top.size());
    for (int i = 1; i < top.size(); i++) {
      assertTrue(top.get(i - 1).frequency() >= top.get(i).frequency());
    }
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.MINUTES)
  void toSortedListVeryLargePerformance() {
    List<WordFrequency> veryLargeDataset = IntStream.range(0, VERY_LARGE_DATA_SIZE)
        .mapToObj(i -> new WordFrequency(generateRandomWord(5, 12), random.nextInt(10_000)))
        .collect(Collectors.toList());
    FixedSizeMinHeap heap = new FixedSizeMinHeap(TOP_N);
    for (WordFrequency wf : veryLargeDataset) {
      heap.add(wf);
    }
    long startTime = System.nanoTime();
    List<WordFrequency> top = heap.toSortedList();
    long duration = System.nanoTime() - startTime;
    System.out.printf("FixedSizeMinHeap: toSortedList() for %,d elements (from %,d input) took %d ms\n",
        TOP_N, VERY_LARGE_DATA_SIZE, TimeUnit.NANOSECONDS.toMillis(duration));
    assertEquals(TOP_N, top.size());
  }

  @Test
  @Timeout(value = 3, unit = TimeUnit.MINUTES)
  void compareWithPriorityQueueVeryLargeEfficiency() {
    List<WordFrequency> veryLargeDataset = IntStream.range(0, VERY_LARGE_DATA_SIZE)
        .mapToObj(i -> new WordFrequency(generateRandomWord(5, 12), random.nextInt(10_000)))
        .collect(Collectors.toList());
    // FixedSizeMinHeap
    FixedSizeMinHeap heap = new FixedSizeMinHeap(TOP_N);
    long startHeap = System.nanoTime();
    for (WordFrequency wf : veryLargeDataset) {
      heap.add(wf);
    }
    List<WordFrequency> heapTop = heap.toSortedList();
    long heapDuration = System.nanoTime() - startHeap;

    // PriorityQueue
    PriorityQueue<WordFrequency> pq = new PriorityQueue<>(TOP_N);
    long startPQ = System.nanoTime();
    for (WordFrequency wf : veryLargeDataset) {
      if (pq.size() < TOP_N) {
        pq.add(wf);
      } else if (wf.compareTo(pq.peek()) > 0) {
        pq.poll();
        pq.add(wf);
      }
    }
    List<WordFrequency> pqTop = new ArrayList<>(pq);
    Collections.sort(pqTop, Collections.reverseOrder());
    long pqDuration = System.nanoTime() - startPQ;

    System.out.printf("FixedSizeMinHeap: %,d elements, top %,d in %d ms\n", VERY_LARGE_DATA_SIZE, TOP_N,
        TimeUnit.NANOSECONDS.toMillis(heapDuration));
    System.out.printf("PriorityQueue: %,d elements, top %,d in %d ms\n", VERY_LARGE_DATA_SIZE, TOP_N,
        TimeUnit.NANOSECONDS.toMillis(pqDuration));
    if (pqDuration > 0) {
      double speedup = (double) pqDuration / heapDuration;
      System.out.printf(
          "Custom FixedSizeMinHeap is %.2fx faster than PriorityQueue implementation\n", speedup);
    } else {
      System.out.println("PriorityQueue time is 0");
    }

    assertEquals(TOP_N, heapTop.size());
    assertEquals(TOP_N, pqTop.size());
    List<Integer> heapFreqs = heapTop.stream().map(WordFrequency::frequency)
        .sorted(Collections.reverseOrder()).collect(Collectors.toList());
    List<Integer> pqFreqs = pqTop.stream().map(WordFrequency::frequency)
        .sorted(Collections.reverseOrder()).collect(Collectors.toList());
    assertEquals(heapFreqs, pqFreqs);
  }
}
