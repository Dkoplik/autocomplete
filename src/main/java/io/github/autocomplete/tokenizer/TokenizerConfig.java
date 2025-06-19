package io.github.autocomplete.tokenizer;

import java.util.function.Predicate;

/**
 * Конфигурация токенизатора с настраиваемыми параметрами
 */
public class TokenizerConfig {
    private final String splitRegex;
    private final Predicate<Character> charFilter;
    private final boolean toLowerCase;

    /**
     * Конфигурация токенизатора со стандартными настройками.
     * <p>
     * Разделение происходит по пробельным символам. В итоговые токены (слова) идут только буквенные
     * символы. Все токены приводятся к нижнему регистру.
     */
    public TokenizerConfig() {
        this("\\s+", Character::isLetter, true);
    }

    /**
     * Конфигурация токенизатора со своими настройками
     * 
     * @param splitRegex Regex выражение, по которому происходит разделение на слова
     * @param charFilter Предикат для фильтрации, выбирает, какие символы оставлять
     * @param toLowerCase Приводить ли все слова к нижнему регистру или оставить как есть
     */
    public TokenizerConfig(String splitRegex, Predicate<Character> charFilter,
            boolean toLowerCase) {
        this.splitRegex = splitRegex;
        this.charFilter = charFilter;
        this.toLowerCase = toLowerCase;
    }

    /**
     * @return Regex выражение, по которому происходит разделение на слова
     */
    public String getSplitRegex() {
        return splitRegex;
    }

    /**
     * @return Предикат для фильтрации, выбирает, какие символы оставлять
     */
    public Predicate<Character> getCharFilter() {
        return charFilter;
    }

    /**
     * @return True, если приводит все слова к нижнему регистру
     */
    public boolean isToLowerCase() {
        return toLowerCase;
    }
}
