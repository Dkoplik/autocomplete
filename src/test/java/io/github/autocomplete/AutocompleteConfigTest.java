package io.github.autocomplete;

import io.github.autocomplete.util.Levenshtein;
import org.junit.jupiter.api.Test;
import java.util.function.BiFunction;
import static org.junit.jupiter.api.Assertions.*;

class AutocompleteConfigTest {
  @Test
  void defaultConstructorSetsExpectedValues() {
    AutocompleteConfig config = new AutocompleteConfig();
    assertNotNull(config.distanceFunction());
    assertEquals(0, config.toleranceThreshold());
    assertEquals(0, config.tolerance());
    assertEquals(0.5, config.similarWeight());
    assertEquals(1.0, config.originalWeight());
    assertEquals(3, config.distanceFunction().apply("kitten", "sitting")); // Is Levenshtein
                                                                           // distance?
  }

  @Test
  void parameterizedConstructorSetsProvidedValues() {
    BiFunction<String, String, Integer> fn = (a, b) -> 42;
    AutocompleteConfig config = new AutocompleteConfig(fn, 2, 3, 0.7, 2.0);
    assertEquals(fn, config.distanceFunction());
    assertEquals(2, config.toleranceThreshold());
    assertEquals(3, config.tolerance());
    assertEquals(0.7, config.similarWeight());
    assertEquals(2.0, config.originalWeight());
  }

  @Test
  void constructorNullDistanceFunctionThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new AutocompleteConfig(null, 1, 1, 0.5, 1.0));
  }

  @Test
  void constructorNegativeToleranceThresholdThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new AutocompleteConfig(Levenshtein::distance, -1, 1, 0.5, 1.0));
  }

  @Test
  void constructorNegativeToleranceThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new AutocompleteConfig(Levenshtein::distance, 1, -1, 0.5, 1.0));
  }
}
