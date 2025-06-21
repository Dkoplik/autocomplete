package io.github.autocomplete.tokenizer;

import java.util.stream.Stream;

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
   * @return Stream токенов
   */
  @Override
  public Stream<String> tokenize(String text) {
    if (text == null)
      return Stream.empty();

    return Stream.of(text.split(config.splitRegex())).map(this::processWord)
        .filter(word -> !word.isEmpty());
  }

  /**
   * Обработать полученное слово, убрав знаки пунктуации и прочие символы
   * 
   * @param word Слово для обработки
   * @return Обработанное слово
   */
  private String processWord(String word) {
    return word.chars().mapToObj(c -> (char) c).filter(c -> config.charFilter().test(c))
        .map(c -> config.toLowerCase() ? Character.toLowerCase(c) : c)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
  }

  /**
   * Установить настройки токенизации
   * 
   * @param config Настройки токенизации
   */
  @Override
  public void setConfig(TokenizerConfig config) {
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
