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
   */
  Stream<String> tokenize(String text);

  /**
   * Установить настройки токенизации
   */
  void setConfig(TokenizerConfig config);

  /**
   * Получить текущие настройки токенизации
   */
  TokenizerConfig getConfig();
}
