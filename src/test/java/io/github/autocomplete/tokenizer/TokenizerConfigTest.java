package io.github.autocomplete.tokenizer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class TokenizerConfigTest {

  @Test
  void defaultConstructor_SetsExpectedValues() {
    TokenizerConfig config = new TokenizerConfig();

    assertEquals("\\s+", config.getSplitRegex());
    assertTrue(config.getCharFilter().test('a'));
    assertFalse(config.getCharFilter().test('1'));
    assertTrue(config.isToLowerCase());
  }

  @Test
  void parameterizedConstructor_SetsProvidedValues() {
    Predicate<Character> customFilter = c -> c == 'a';
    TokenizerConfig config = new TokenizerConfig(",", customFilter, false);

    assertEquals(",", config.getSplitRegex());
    assertSame(customFilter, config.getCharFilter());
    assertFalse(config.isToLowerCase());
  }
}
