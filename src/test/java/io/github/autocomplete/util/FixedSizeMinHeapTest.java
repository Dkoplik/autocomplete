package io.github.autocomplete.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixedSizeMinHeapTest {

  private FixedSizeMinHeap heap;

  @BeforeEach
  void setUp() {
    heap = new FixedSizeMinHeap(3);
  }

  @Test
  void addWhenHeapNotFullAddsElements() {
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("B", 3));
    heap.add(new WordFrequency("C", 7));

    List<WordFrequency> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertEquals("C", result.get(0).word());
    assertEquals("A", result.get(1).word());
    assertEquals("B", result.get(2).word());
  }

  @Test
  void addWhenNewElementLargerThanMinReplacesMin() {
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("B", 3));
    heap.add(new WordFrequency("C", 7));
    heap.add(new WordFrequency("D", 6));

    List<WordFrequency> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertEquals("C", result.get(0).word());
    assertEquals("D", result.get(1).word());
    assertEquals("A", result.get(2).word());
  }

  @Test
  void addWhenNewElementSmallerThanMinKeepsHeap() {
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("B", 3));
    heap.add(new WordFrequency("C", 7));
    heap.add(new WordFrequency("D", 2));

    List<WordFrequency> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertFalse(result.stream().anyMatch(c -> "D".equals(c.word())));
  }

  @Test
  void addWithSameFrequencyDifferentWordsOrdersCorrectly() {
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("B", 5));
    heap.add(new WordFrequency("C", 5));

    List<WordFrequency> result = heap.toSortedList();
    assertEquals("A", result.get(0).word());
    assertEquals("B", result.get(1).word());
    assertEquals("C", result.get(2).word());
  }

  @Test
  void toSortedListReturnsDescendingOrder() {
    heap.add(new WordFrequency("C", 7));
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("B", 3));

    List<WordFrequency> result = heap.toSortedList();
    assertEquals(7, result.get(0).frequency());
    assertEquals(5, result.get(1).frequency());
    assertEquals(3, result.get(2).frequency());
  }

  @Test
  void addWhenHeapSizeZeroIgnoresElements() {
    FixedSizeMinHeap emptyHeap = new FixedSizeMinHeap(0);
    emptyHeap.add(new WordFrequency("A", 5));
    assertTrue(emptyHeap.toSortedList().isEmpty());
  }

  @Test
  void addWhenHeapSizeOneMaintainsSingleElement() {
    FixedSizeMinHeap singleHeap = new FixedSizeMinHeap(1);
    singleHeap.add(new WordFrequency("A", 5));
    singleHeap.add(new WordFrequency("B", 10));

    List<WordFrequency> result = singleHeap.toSortedList();
    assertEquals(1, result.size());
    assertEquals("B", result.get(0).word());
  }

  @Test
  void addWithEqualElementsHandlesCorrectly() {
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("A", 5));
    heap.add(new WordFrequency("B", 5));

    List<WordFrequency> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertEquals("A", result.get(0).word());
    assertEquals("A", result.get(1).word());
    assertEquals("B", result.get(2).word());
  }
}
