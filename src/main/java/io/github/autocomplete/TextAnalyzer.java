package io.github.autocomplete;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import io.github.autocomplete.tokenizer.SimpleTokenizer;
import io.github.autocomplete.tokenizer.Tokenizer;
import io.github.autocomplete.util.WordFrequency;

/**
 * Анализатор текста для подсчёта частоты слов и их поиска. Позволяет указать токенизатор по
 * умолчанию, и в то же время позволяет использовать любой другой токенизатор для особых случаев.
 */
public class TextAnalyzer {
  private final Trie trie = new Trie();
  private final Tokenizer tokenizer;

  /**
   * Создать анализатор текста с {@link SimpleTokenizer} в качестве токенизатора по умолчанию
   */
  public TextAnalyzer() {
    this(new SimpleTokenizer());
  }

  /**
   * Создать анализатор текста с указанным токенизатором в качестве токенизатора по умолчанию
   * 
   * @param tokenizer Токенизатор для обработки текста
   */
  public TextAnalyzer(Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  /**
   * Обрабатывает текст и обновляет частоты слов, используя токенизатор по умолчанию
   * 
   * @param text Текст для обработки
   */
  public void addText(String text) {
    addText(text, tokenizer);
  }

  /**
   * Обрабатывает текст и обновляет частоты слов, используя указанный токенизатор
   * 
   * @param text Текст для обработки
   * @param tokenizer Токенизатор для обработки текста
   */
  public void addText(String text, Tokenizer tokenizer) {
    tokenizer.tokenize(text).forEach(trie::insert);
  }

  /**
   * Получить частоту конкретного слова
   * 
   * @param word Слово, для которого нужно получить частоту
   * @return Частота указанного слова
   */
  public int getWordFrequency(String word) {
    return trie.getFrequency(word);
  }

  /**
   * Получить топ-N самых частых слов
   * 
   * @param n Количество слов
   * @return Список из топ-N самых частых слов
   */
  public List<WordFrequency> getTopWords(int n) {
    return trie.getTopFrequentWords(n);
  }

  /**
   * @return Все слова с их частотами
   */
  public Map<String, Integer> getAllWords() {
    return trie.getAllWords();
  }

  /**
   * Возвращает используемый по умолчанию токенизатор
   * 
   * @return Токенизатор
   */
  public Tokenizer getTokenizer() {
    return tokenizer;
  }

  /**
   * Удаляет слово из анализатора
   * 
   * @param word Удаляемое слово
   */
  public void removeWord(String word) {
    trie.remove(word);
  }

  /**
   * Возвращает слова, соответствующие regex-шаблону
   * 
   * @param regex Regex для отбора слов
   * @return Слова и их частоты, удовлетворяющие regex-шаблону
   */
  public Map<String, Integer> getWordsByRegex(String regex) {
    Pattern pattern = Pattern.compile(regex);
    return trie.getAllWords().entrySet().stream()
        .filter(entry -> pattern.matcher(entry.getKey()).matches())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Удаляет все слова из анализатора
   */
  public void clear() {
    trie.clear();
  }

  /**
   * Возвращает префиксное дерево (для внутреннего использования)
   * 
   * @return Префиксное дерево {@link Trie}
   */
  Trie getTrie() {
    return trie;
  }
}
