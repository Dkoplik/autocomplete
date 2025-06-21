package io.github.autocomplete.tokenizer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SimpleTokenizerTest {

  private SimpleTokenizer tokenizer;

  @BeforeEach
  void setUp() {
    tokenizer = new SimpleTokenizer();
  }

  @Test
  void tokenize_NullInput_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> tokenizer.tokenize(null));
  }

  @Test
  void tokenize_EmptyInput_ReturnsEmptyStream() {
    assertTrue(tokenizer.tokenize("").collect(Collectors.toList()).isEmpty());
  }

  @Test
  void tokenize_WithDefaultConfig_SplitsAndFiltersCorrectly() {
    String input = "Hello, World! This is a test.";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    assertEquals(List.of("hello", "world", "this", "is", "a", "test"), tokens);
  }

  @Test
  void tokenize_WithNumbersAndSymbols_FiltersCorrectly() {
    String input = "Price: $19.99 - 50% off!";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    assertEquals(List.of("price", "off"), tokens);
  }

  @Test
  void tokenize_WithCustomSplitRegex_SplitsCorrectly() {
    TokenizerConfig config = new TokenizerConfig("[- ]", Character::isLetter, true);
    tokenizer.setConfig(config);

    String input = "email user-NAME Domain-com";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    assertEquals(List.of("email", "user", "name", "domain", "com"), tokens);
  }

  @Test
  void tokenize_WithCustomCharFilter_IncludesNumbers() {
    TokenizerConfig config =
        new TokenizerConfig("\\s+", c -> Character.isLetter(c) || Character.isDigit(c), true);
    tokenizer.setConfig(config);

    String input = "Item123 45.6% price_$99";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    assertEquals(List.of("item123", "456", "price99"), tokens);
  }

  @Test
  void tokenize_WithoutLowerCase_KeepsOriginalCase() {
    TokenizerConfig config = new TokenizerConfig("\\s+", Character::isLetter, false);
    tokenizer.setConfig(config);

    String input = "Hello WORLD CamelCase";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    assertEquals(List.of("Hello", "WORLD", "CamelCase"), tokens);
  }

  @Test
  void tokenize_WithMixedCharacters_ProcessesCorrectly() {
    String input = "  Extra   spaces...  \nNew\tLines!  ";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    assertEquals(List.of("extra", "spaces", "new", "lines"), tokens);
  }

  @ParameterizedTest
  @ValueSource(strings = {"!@#$%^&*()", "123.456", "  \t\n  "})
  void tokenize_NoValidCharacters_ReturnsEmptyStream(String input) {
    assertTrue(tokenizer.tokenize(input).collect(Collectors.toList()).isEmpty());
  }

  @Test
  void tokenize_WithUnicodeCharacters_HandlesCorrectly() {
    String input = "Привет! こんにちは 안녕하세요";
    List<String> tokens = tokenizer.tokenize(input).collect(Collectors.toList());

    // Проверяем что не-латинские буквы сохраняются
    assertEquals(List.of("привет", "こんにちは", "안녕하세요"), tokens);
  }

  @Test
  void setConfig_ChangesTokenizerBehavior() {
    // Исходная конфигурация
    String input = "Test123";
    List<String> initialTokens = tokenizer.tokenize(input).collect(Collectors.toList());
    assertEquals(List.of("test"), initialTokens);

    // Меняем конфигурацию
    TokenizerConfig newConfig =
        new TokenizerConfig("\\s+", c -> Character.isLetterOrDigit(c), false);
    tokenizer.setConfig(newConfig);

    List<String> newTokens = tokenizer.tokenize(input).collect(Collectors.toList());
    assertEquals(List.of("Test123"), newTokens);
  }

  @Test
  void getConfig_ReturnsCurrentConfiguration() {
    TokenizerConfig defaultConfig = tokenizer.getConfig();
    assertEquals("\\s+", defaultConfig.splitRegex());
    assertTrue(defaultConfig.toLowerCase());

    TokenizerConfig newConfig = new TokenizerConfig(",", Character::isDigit, false);
    tokenizer.setConfig(newConfig);

    TokenizerConfig retrievedConfig = tokenizer.getConfig();
    assertEquals(",", retrievedConfig.splitRegex());
    assertFalse(retrievedConfig.toLowerCase());
  }
}
