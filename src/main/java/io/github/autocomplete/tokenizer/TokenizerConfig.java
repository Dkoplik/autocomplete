package io.github.autocomplete.tokenizer;

import java.util.function.Predicate;

/**
 * Конфигурация токенизатора с настраиваемыми параметрами.
 *
 * @param splitRegex Regex выражение, по которому происходит разделение текста на слова (токены). По
 *        умолчанию - пробельные символы.
 *
 * @param charFilter Предикат для фильтрации, выбирает, какие символы оставлять в итоговых токенах.
 *        По умолчанию - только буквенные символы.
 *
 * @param toLowerCase Приводить ли все слова к нижнему регистру. По умолчанию - true.
 */
public record TokenizerConfig(String splitRegex, Predicate<Character> charFilter,
    boolean toLowerCase) {
  /**
   * Конфигурация токенизатора со стандартными настройками.
   *
   * <p>
   * Разделение происходит по пробельным символам. В итоговые токены (слова) идут только буквенные
   * символы. Все токены приводятся к нижнему регистру.
   * </p>
   */
  public TokenizerConfig() {
    this("\\s+", Character::isLetter, true);
  }
}
