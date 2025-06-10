package io.github.autocomplete.tokenizer;

import java.util.stream.Stream;

/**
 * Реализация токенизатора с настройками фильтрации
 */
public class SimpleTokenizer implements Tokenizer {
  private TokenizerConfig config = new TokenizerConfig();

  /**
   * Разбить текст на токены
   * 
   * @param text Текст, который необходимо разбить на токены
   */
  @Override
  public Stream<String> tokenize(String text) {
    if (text == null)
      return Stream.empty();

    return Stream.of(text.split(config.getSplitRegex())).map(this::processWord)
        .filter(word -> !word.isEmpty());
  }

  /**
   * Обработать полученное слово, убрав знаки пунктуации и прочие символы.
   * 
   * @param word Слово для обработки
   */
  private String processWord(String word) {
    StringBuilder sb = new StringBuilder();
    for (char c : word.toCharArray()) {
      if (config.getCharFilter().test(c)) {
        sb.append(config.isToLowerCase() ? Character.toLowerCase(c) : c);
      }
    }
    return sb.toString();
  }

  /**
   * Установить настройки токенизации
   */
  @Override
  public void setConfig(TokenizerConfig config) {
    this.config = config;
  }

  /**
   * Получить текущие настройки токенизации
   */
  @Override
  public TokenizerConfig getConfig() {
    return config;
  }
}
