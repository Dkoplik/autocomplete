# autocomplete
Библиотека для работы с текстом и автозавершениями. Предоставляет простой текстовый анализатор и автозаполнения, причём для автозаполнений присутсвует опциональное LRU-кеширование.

## Технологии
- Java 21
- Gradle
- JUnit5 для тестирования

## Основные возможности
- Токенизация и анализ поступаемого текста с подсчётом частот слов
- Поддержка кастомных токенизаторов
- Предложение автозаполнений на основе частот из текстового анализатора
- Поддержка опционального LRU-кеширования для запросов на автозаполнение

## Основные классы
- `AutocompleteProvider` - предоставляет автозаполнения на основе частот слов из предоставленного текста.
- `TextAnalyzer` - текстовый анализатор текста, проводит токенизацию и подсчёт частот слов.
- `Tokenizer` - интерфейс токенизатора для `TextAnalyzer`, на случай если работы стандартного токенизатора недостаточно.

## Требования
- Java 21 или выше
- Gradle (в репозитории присутствует Wrapper)

## Сборка/разработка библиотеки

### Команда для сборки
```bash
./gradlew build
```

После этого появятся следующие файлы:
- `build/libs/autocomplete-1.0-SNAPSHOT.jar` - главный JAR с библиотекой
- `build/libs/autocomplete-1.0-SNAPSHOT-sources.jar` - JAR с исходниками (для IDE)
- `build/libs/autocomplete-1.0-SNAPSHOT-javadoc.jar` - JAR с документацией

### Запуск тестов
```bash
./gradlew test
```

### Запуск стресс-тестирования
```bash
./gradlew stressTest
```

## Пример использования библиотеки

```java
import io.github.autocomplete.AutocompleteProvider;
import io.github.autocomplete.TextAnalyzer;
import io.github.autocomplete.model.Candidate;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        // Создать текстовый анализатор
        TextAnalyzer analyzer = new TextAnalyzer();
        
        // Пропарсить какой-то текст
        analyzer.addText("Hello world! This is a test. Hello again.");
        
        // Создать AutocompleteProvider для получения вариантов завершения слов
        AutocompleteProvider provider = new AutocompleteProvider(analyzer);
        
        // Получить варианты завершения префикса
        List<Candidate> suggestions = provider.getAutocomplete("he", 5);
        
        // Вывод кандидатов на завершение слова с их весами
        for (Candidate candidate : suggestions) {
            System.out.println(candidate.word() + " (weight: " + candidate.weight() + ")");
        }
    }
}
``` 
