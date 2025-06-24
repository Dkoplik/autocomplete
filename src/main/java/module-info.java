/**
 * Модуль для работы с текстом и автозавершениями.
 * 
 * <p>Предоставляет простой текстовый анализатор и автозаполнения, 
 * причём для автозаполнений присутствует опциональное LRU-кеширование.</p>
 * 
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Токенизация и анализ поступаемого текста с подсчётом частот слов</li>
 *   <li>Поддержка кастомных токенизаторов</li>
 *   <li>Предложение автозаполнений на основе частот из текстового анализатора</li>
 *   <li>Поддержка опционального LRU-кеширования для запросов на автозаполнение</li>
 * </ul>
 * 
 * <p>Основные классы:</p>
 * <ul>
 *   <li>{@link io.github.autocomplete.AutocompleteProvider} - предоставляет автозаполнения
 *   на основе частот слов из предоставленного текста</li>
 *   <li>{@link io.github.autocomplete.TextAnalyzer} - текстовый анализатор текста, проводит
 *   токенизацию и подсчёт частот слов</li>
 *   <li>{@link io.github.autocomplete.tokenizer.Tokenizer} - интерфейс токенизатора для TextAnalyzer,
 *   на случай если работы стандартного токенизатора недостаточно</li>
 * </ul>
 * 
 * @since 1.0.1
 */
module io.github.autocomplete {
  exports io.github.autocomplete;
  
  exports io.github.autocomplete.config;

  exports io.github.autocomplete.distance;
  
  exports io.github.autocomplete.model;
  
  exports io.github.autocomplete.tokenizer;
}
