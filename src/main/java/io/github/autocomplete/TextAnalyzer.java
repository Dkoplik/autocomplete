package io.github.autocomplete;

import io.github.autocomplete.tokenizer.SimpleTokenizer;
import io.github.autocomplete.tokenizer.Tokenizer;
import io.github.autocomplete.util.WordFrequency;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Анализатор текста для подсчёта частоты слов и их поиска. Позволяет указать токенизатор по
 * умолчанию, и в то же время позволяет использовать любой другой токенизатор для особых случаев.
 */
public class TextAnalyzer {
  private final Trie trie = new Trie();
  private final Tokenizer tokenizer;

  /**
   * Создать анализатор текста с {@link SimpleTokenizer} в качестве токенизатора по умолчанию.
   */
  public TextAnalyzer() {
    this(new SimpleTokenizer());
  }

  /**
   * Создать анализатор текста с указанным токенизатором в качестве токенизатора по умолчанию.
   *
   * @param tokenizer Токенизатор для обработки текста
   *
   * @throws IllegalArgumentException Если tokenizer равен null
   */
  public TextAnalyzer(Tokenizer tokenizer) {
    if (tokenizer == null) {
      throw new IllegalArgumentException("tokenizer cannot be null");
    }

    this.tokenizer = tokenizer;
  }

  /**
   * Обрабатывает текст и обновляет частоты слов, используя токенизатор по умолчанию.
   *
   * @param text Текст для обработки
   *
   * @throws IllegalArgumentException Если text равен null
   */
  public void addText(String text) {
    addText(text, tokenizer);
  }

  /**
   * Обрабатывает текст и обновляет частоты слов, используя указанный токенизатор.
   *
   * @param text Текст для обработки
   *
   * @param tokenizer Токенизатор для обработки текста
   *
   * @throws IllegalArgumentException Если text равен null
   *
   * @throws IllegalArgumentException Если tokenizer равен null
   */
  public void addText(String text, Tokenizer tokenizer) {
    if (text == null) {
      throw new IllegalArgumentException("text cannot be null");
    }
    if (tokenizer == null) {
      throw new IllegalArgumentException("tokenizer cannot be null");
    }

    tokenizer.tokenize(text).forEach(trie::insert);
  }

  /**
   * Получить частоту конкретного слова.
   *
   * @param word Слово, для которого нужно получить частоту
   *
   * @return Частота указанного слова
   *
   * @throws IllegalArgumentException Если word равен null или пустой строке
   */
  public int getWordFrequency(String word) {
    if (word == null || word.isEmpty()) {
      throw new IllegalArgumentException("word cannot be null or empty");
    }

    return trie.getFrequency(word);
  }

  /**
   * Получить топ-N самых частых слов.
   *
   * @param n Количество слов
   *
   * @return Список из топ-N самых частых слов
   *
   * @throws IllegalArgumentException Если n меньше или равно 0
   */
  public List<WordFrequency> getTopWords(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n cannot be less than or equal to 0");
    }

    return trie.getTopFrequentWords(n);
  }

  /**
   * Возвращает все слова с их частотами.
   *
   * @return Все слова с их частотами
   */
  public Map<String, Integer> getAllWords() {
    return trie.getAllWords();
  }

  /**
   * Возвращает используемый по умолчанию токенизатор.
   *
   * @return Токенизатор
   */
  public Tokenizer getTokenizer() {
    return tokenizer;
  }

  /**
   * Удаляет слово из анализатора.
   *
   * @param word Удаляемое слово
   *
   * @throws IllegalArgumentException Если word равен null или пустой строке
   */
  public void removeWord(String word) {
    if (word == null || word.isEmpty()) {
      throw new IllegalArgumentException("word cannot be null or empty");
    }

    trie.remove(word);
  }

  /**
   * Возвращает слова, соответствующие regex-шаблону.
   *
   * @param regex Regex для отбора слов
   *
   * @return Слова и их частоты, удовлетворяющие regex-шаблону
   *
   * @throws IllegalArgumentException Если regex равен null или пустой строке
   */
  public Map<String, Integer> getWordsByRegex(String regex) {
    if (regex == null || regex.isEmpty()) {
      throw new IllegalArgumentException("regex cannot be null or empty");
    }

    Pattern pattern = Pattern.compile(regex);
    return trie.getAllWords().entrySet().stream()
        .filter(entry -> pattern.matcher(entry.getKey()).matches())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Удаляет все слова из анализатора.
   */
  public void clear() {
    trie.clear();
  }

  /**
   * Возвращает префиксное дерево (для внутреннего использования).
   *
   * @return Префиксное дерево {@link Trie}
   */
  Trie getTrie() {
    return trie;
  }
}
