package io.github.autocomplete.tokenizer;

import java.util.stream.Stream;
import io.github.autocomplete.config.TokenizerConfig;

/**
 * Реализация токенизатора с настраиваемыми параметрами через {@link TokenizerConfig}
 */
public class SimpleTokenizer implements Tokenizer {
  private TokenizerConfig config;

  /**
   * Создает токенизатор с настройками по умолчанию {@link TokenizerConfig}
   */
  public SimpleTokenizer() {
    this(new TokenizerConfig());
  }

  /**
   * Создает токенизатор с указанными настройками
   * 
   * @param config Настройки токенизации {@link TokenizerConfig}
   */
  public SimpleTokenizer(TokenizerConfig config) {
    this.config = config;
  }

  /**
   * Разбить текст на токены
   * 
   * @param text Текст, который необходимо разбить на токены
   * 
   * @return Stream токенов
   * 
   * @throws IllegalArgumentException Если text равен null
   */
  @Override
  public Stream<String> tokenize(String text) {
    if (text == null) {
      throw new IllegalArgumentException("text cannot be null");
    }

    return Stream.of(text.split(config.splitRegex())).map(this::processWord)
        .filter(word -> !word.isEmpty());
  }

  /**
   * Обработать полученное слово, убрав знаки пунктуации и прочие символы
   * 
   * @param word Слово для обработки
   * 
   * @return Обработанное слово
   * 
   * @throws IllegalArgumentException Если word равен null
   */
  private String processWord(String word) {
    if (word == null) {
      throw new IllegalArgumentException("word cannot be null");
    }
    return word.chars().mapToObj(c -> (char) c).filter(c -> config.charFilter().test(c))
        .map(c -> config.toLowerCase() ? Character.toLowerCase(c) : c)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
  }

  /**
   * Установить настройки токенизации
   * 
   * @param config Настройки токенизации
   * 
   * @throws IllegalArgumentException Если config равен null
   */
  @Override
  public void setConfig(TokenizerConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("config cannot be null");
    }

    this.config = config;
  }

  /**
   * Получить текущие настройки токенизации
   * 
   * @return Текущие настройки токенизации
   */
  @Override
  public TokenizerConfig getConfig() {
    return config;
  }
}
