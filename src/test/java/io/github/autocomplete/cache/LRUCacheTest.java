package io.github.autocomplete.cache;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LRUCacheTest {

  private LRUCache<Integer, String> cache;

  @BeforeEach
  void setUp() {
    cache = new LRUCache<>(3);
  }

  @Test
  void getWhenKeyExistsReturnsValueAndUpdatesRecency() {
    cache.put(1, "One");
    cache.put(2, "Two");

    assertEquals("One", cache.get(1));

    cache.put(3, "Three");
    cache.put(4, "Four");

    assertNull(cache.get(2));
    assertEquals("One", cache.get(1));
    assertEquals("Four", cache.get(4));
  }

  @Test
  void getWhenKeyMissingReturnsNull() {
    assertNull(cache.get(1));
    cache.put(1, "One");
    assertNull(cache.get(2));
  }

  @Test
  void putWhenCacheNotFullAddsElements() {
    cache.put(1, "One");
    cache.put(2, "Two");

    assertEquals("One", cache.get(1));
    assertEquals("Two", cache.get(2));
  }

  @Test
  void putWhenCacheFullEvictsLeastRecentlyUsed() {
    cache.put(1, "One");
    cache.put(2, "Two");
    cache.put(3, "Three");

    assertEquals("One", cache.get(1));
    assertEquals("Two", cache.get(2));
    assertEquals("Three", cache.get(3));

    cache.put(4, "Four");

    assertNull(cache.get(1));
    assertEquals("Four", cache.get(4));
  }

  @Test
  void putWhenKeyExistsUpdatesValueAndRecency() {
    cache.put(1, "One");
    cache.put(2, "Two");

    cache.put(1, "Updated");

    assertEquals("Updated", cache.get(1));

    cache.put(3, "Three");
    cache.put(4, "Four");

    assertNull(cache.get(2));
    assertEquals("Updated", cache.get(1));
  }

  @Test
  void evictionOrderWhenMixedOperationsEvictsCorrectly() {
    cache.put(1, "One");
    cache.put(2, "Two");
    cache.get(1);
    cache.put(3, "Three");

    cache.put(4, "Four");

    assertNull(cache.get(2));
    assertEquals("One", cache.get(1));
    assertEquals("Three", cache.get(3));
    assertEquals("Four", cache.get(4));
  }

  @Test
  void zeroCapacityCacheIgnoresAllPuts() {
    LRUCache<Integer, String> zeroCache = new LRUCache<>(0);
    zeroCache.put(1, "One");
    assertNull(zeroCache.get(1));

    zeroCache.put(2, "Two");
    assertNull(zeroCache.get(2));
  }

  @Test
  void singleItemCacheEvictsProperly() {
    LRUCache<Integer, String> singleCache = new LRUCache<>(1);
    singleCache.put(1, "One");
    assertEquals("One", singleCache.get(1));

    singleCache.put(2, "Two");
    assertNull(singleCache.get(1));
    assertEquals("Two", singleCache.get(2));
  }

  @Test
  void repeatedAccessUpdatesRecencyWithoutEviction() {
    cache.put(1, "One");
    cache.put(2, "Two");
    cache.put(3, "Three");

    cache.get(3);
    cache.get(2);
    cache.get(1);

    cache.put(4, "Four");

    assertNull(cache.get(3));
    assertEquals("One", cache.get(1));
    assertEquals("Two", cache.get(2));
    assertEquals("Four", cache.get(4));
  }

  @Test
  void complexSequenceCorrectlyMaintainsOrder() {
    cache.put(1, "A");
    cache.put(2, "B");
    cache.get(1);
    cache.put(3, "C");
    cache.put(4, "D");

    assertEquals("C", cache.get(3));
    assertEquals("A", cache.get(1));
    assertEquals("D", cache.get(4));
    assertNull(cache.get(2));

    cache.put(5, "E");
    assertEquals("D", cache.get(4));
    assertEquals("A", cache.get(1));
    assertEquals("E", cache.get(5));
    assertNull(cache.get(3));

    cache.put(6, "F");
    assertEquals("A", cache.get(1));
    assertEquals("E", cache.get(5));
    assertEquals("F", cache.get(6));
    assertNull(cache.get(4));
  }

  @Test
  void clearRemovesAllElements() {
    cache.put(1, "One");
    cache.put(2, "Two");
    cache.put(3, "Three");

    cache.clear();

    assertNull(cache.get(1));
    assertNull(cache.get(2));
    assertNull(cache.get(3));
  }

  @Test
  void clearResetsInternalState() {
    cache.put(1, "One");
    cache.put(2, "Two");

    assertEquals("One", cache.get(1));
    assertEquals("Two", cache.get(2));

    cache.clear();

    cache.put(3, "Three");
    cache.put(4, "Four");

    assertEquals("Three", cache.get(3));
    assertEquals("Four", cache.get(4));
    assertNull(cache.get(1));

    cache.put(5, "Five");
    cache.put(6, "Six");
    assertNull(cache.get(3));
  }

  @Test
  void clearOnEmptyCacheDoesNothing() {
    assertDoesNotThrow(() -> cache.clear());

    cache.put(1, "One");
    assertEquals("One", cache.get(1));
  }

  @Test
  void clearResetsAccessOrder() {
    cache.put(1, "One");
    cache.put(2, "Two");
    cache.put(3, "Three");

    cache.get(1);
    cache.get(2);

    cache.clear();

    cache.put(1, "OneNew");
    cache.put(2, "TwoNew");
    cache.put(3, "ThreeNew");

    cache.put(4, "Four");

    assertNull(cache.get(1));
    assertEquals("TwoNew", cache.get(2));
    assertEquals("ThreeNew", cache.get(3));
    assertEquals("Four", cache.get(4));
  }

  @Test
  void clearWithSingleElement() {
    cache.put(1, "One");
    cache.clear();

    assertNull(cache.get(1));
  }

  @Test
  void clearFollowedByOperationsWorksCorrectly() {
    cache.put(1, "One");
    cache.clear();

    cache.put(2, "Two");
    assertEquals("Two", cache.get(2));

    cache.put(3, "Three");
    cache.put(4, "Four");

    assertEquals("Two", cache.get(2));
    assertEquals("Three", cache.get(3));
    assertEquals("Four", cache.get(4));

    cache.put(5, "Five");
    assertNull(cache.get(2));
  }

  @Test
  void multipleClearCallsWorkConsistently() {
    cache.put(1, "One");
    cache.clear();

    cache.put(2, "Two");
    cache.clear();

    assertNull(cache.get(1));
    assertNull(cache.get(2));

    cache.put(3, "Three");
    assertEquals("Three", cache.get(3));
  }
}
