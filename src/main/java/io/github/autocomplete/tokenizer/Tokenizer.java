package io.github.autocomplete.tokenizer;

import java.util.stream.Stream;

/**
 * Интерфейс для токенизатора (разбение текста на слова)
 */
public interface Tokenizer {
  /**
   * Разбить текст на токены
   * 
   * @param text Текст, который необходимо разбить на токены
   * 
   * @return Stream токенов
   * 
   * @throws IllegalArgumentException Если text равен null
   */
  Stream<String> tokenize(String text);

  /**
   * Установить настройки токенизации
   * 
   * @param config Настройки токенизации
   * 
   * @throws IllegalArgumentException Если config равен null
   */
  void setConfig(TokenizerConfig config);

  /**
   * Получить текущие настройки токенизации
   * 
   * @return Текущие настройки токенизации
   */
  TokenizerConfig getConfig();
}
