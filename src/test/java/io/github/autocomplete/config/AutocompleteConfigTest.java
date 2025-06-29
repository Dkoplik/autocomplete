package io.github.autocomplete.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import io.github.autocomplete.distance.Levenshtein;

class AutocompleteConfigTest {
  @Test
  void defaultConstructorSetsExpectedValues() {
    AutocompleteConfig config = new AutocompleteConfig();
    assertNotNull(config.distanceFunction());
    assertEquals(0, config.toleranceThreshold());
    assertEquals(0, config.tolerance());
    assertEquals(0.5, config.similarWeight());
    assertEquals(1.0, config.originalWeight());
    assertEquals(3, config.distanceFunction().apply("kitten", "sitting"));
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
