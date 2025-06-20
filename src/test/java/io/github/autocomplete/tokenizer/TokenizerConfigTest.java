package io.github.autocomplete.tokenizer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

// Есть ли смысл тестировать record TokenizerConfig?
class TokenizerConfigTest {

  @Test
  void defaultConstructor_SetsExpectedValues() {
    TokenizerConfig config = new TokenizerConfig();

    assertEquals("\\s+", config.splitRegex());
    assertTrue(config.charFilter().test('a'));
    assertFalse(config.charFilter().test('1'));
    assertTrue(config.toLowerCase());
  }

  @Test
  void parameterizedConstructor_SetsProvidedValues() {
    Predicate<Character> customFilter = c -> c == 'a';
    TokenizerConfig config = new TokenizerConfig(",", customFilter, false);

    assertEquals(",", config.splitRegex());
    assertSame(customFilter, config.charFilter());
    assertFalse(config.toLowerCase());
  }
}
