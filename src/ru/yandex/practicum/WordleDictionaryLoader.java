package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary load(String fileName) throws DictionaryLoadException {
        File dictionaryFile = new File(fileName);
        if (!dictionaryFile.exists()) {
            String errorMessage = "Файл словаря не найден: " + fileName;
            if (log != null) {
                log.println(errorMessage);
            }
            throw new DictionaryLoadException(errorMessage);
        }

        List<String> loadedWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(dictionaryFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String normalizedWord = WordleDictionary.normalize(line);
                if (normalizedWord.length() == Constants.WORD_LENGTH) {
                    loadedWords.add(normalizedWord);
                }
            }

            if (loadedWords.isEmpty()) {
                String errorMessage = "После фильтрации словарь пуст.";
                if (log != null) {
                    log.println(errorMessage);
                }
                throw new DictionaryLoadException(errorMessage);
            }

            if (log != null) {
                log.println("Словарь загружен. Всего слов: " + loadedWords.size());
            }

            return new WordleDictionary(loadedWords, log);

        } catch (IOException ioException) {
            String errorMessage = "Ошибка чтения файла словаря: " + ioException.getMessage();
            if (log != null) {
                log.println(errorMessage);
            }
            throw new DictionaryLoadException(errorMessage, ioException);
        }
    }
}