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
  void add_WhenHeapNotFull_AddsElements() {
    heap.add(new Candidate("A", 5));
    heap.add(new Candidate("B", 3));
    heap.add(new Candidate("C", 7));

    List<Candidate> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertEquals("C", result.get(0).word());
    assertEquals("A", result.get(1).word());
    assertEquals("B", result.get(2).word());
  }

  @Test
  void add_WhenNewElementLargerThanMin_ReplacesMin() {
    heap.add(new Candidate("A", 5));
    heap.add(new Candidate("B", 3));
    heap.add(new Candidate("C", 7));
    heap.add(new Candidate("D", 6)); 

    List<Candidate> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertEquals("C", result.get(0).word());
    assertEquals("D", result.get(1).word());
    assertEquals("A", result.get(2).word());
  }

  @Test
  void add_WhenNewElementSmallerThanMin_KeepsHeap() {
    heap.add(new Candidate("A", 5));
    heap.add(new Candidate("B", 3));
    heap.add(new Candidate("C", 7));
    heap.add(new Candidate("D", 2)); 

    List<Candidate> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertFalse(result.stream().anyMatch(c -> "D".equals(c.word())));
  }

  @Test
  void add_WithSameFrequencyDifferentWords_OrdersCorrectly() {
    heap.add(new Candidate("A", 5));
    heap.add(new Candidate("B", 5));
    heap.add(new Candidate("C", 5));

    List<Candidate> result = heap.toSortedList();
    assertEquals("A", result.get(0).word());
    assertEquals("B", result.get(1).word());
    assertEquals("C", result.get(2).word());
  }

  @Test
  void toSortedList_ReturnsDescendingOrder() {
    heap.add(new Candidate("C", 7));
    heap.add(new Candidate("A", 5));
    heap.add(new Candidate("B", 3));

    List<Candidate> result = heap.toSortedList();
    assertEquals(7, result.get(0).frequency());
    assertEquals(5, result.get(1).frequency());
    assertEquals(3, result.get(2).frequency());
  }

  @Test
  void add_WhenHeapSizeZero_IgnoresElements() {
    FixedSizeMinHeap emptyHeap = new FixedSizeMinHeap(0);
    emptyHeap.add(new Candidate("A", 5));
    assertTrue(emptyHeap.toSortedList().isEmpty());
  }

  @Test
  void add_WhenHeapSizeOne_MaintainsSingleElement() {
    FixedSizeMinHeap singleHeap = new FixedSizeMinHeap(1);
    singleHeap.add(new Candidate("A", 5));
    singleHeap.add(new Candidate("B", 10));

    List<Candidate> result = singleHeap.toSortedList();
    assertEquals(1, result.size());
    assertEquals("B", result.get(0).word());
  }

  @Test
  void add_WithEqualElements_HandlesCorrectly() {
    heap.add(new Candidate("A", 5));
    heap.add(new Candidate("A", 5)); 
    heap.add(new Candidate("B", 5));

    List<Candidate> result = heap.toSortedList();
    assertEquals(3, result.size());
    assertEquals("A", result.get(0).word());
    assertEquals("A", result.get(1).word());
    assertEquals("B", result.get(2).word());
  }
}
