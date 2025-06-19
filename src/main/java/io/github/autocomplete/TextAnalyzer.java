package io.github.autocomplete;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import io.github.autocomplete.tokenizer.SimpleTokenizer;
import io.github.autocomplete.tokenizer.Tokenizer;
import io.github.autocomplete.util.Candidate;

/**
 * Анализатор текста для подсчёта частоты слов и их поиска
 */
public class TextAnalyzer {
  private final Trie trie = new Trie();
  private final Tokenizer tokenizer;

  public TextAnalyzer() {
    this(new SimpleTokenizer());
  }

  /**
   * Создать анализатор текста с используемым токенизатором
   * 
   * @param tokenizer Используемый токенизатор для обработки текста
   */
  public TextAnalyzer(Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  /**
   * Обработать текст и обновить частоты слов
   * 
   * @param text Текст для обработки
   */
  public void processText(String text) {
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
  public List<Candidate> getTopWords(int n) {
    return trie.getTopFrequentWords(n);
  }

  /**
   * @return Все слова с их частотами
   */
  public Map<String, Integer> getAllWords() {
    return trie.getAllWords();
  }

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
   * Возвращает автодополнения (для внутреннего использования)
   */
  List<Candidate> getCompletions(String prefix, int limit) {
    return trie.findCompletions(prefix, limit);
  }
}
