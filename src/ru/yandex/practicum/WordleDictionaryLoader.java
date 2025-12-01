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
        File file = new File(fileName);
        if (!file.exists()) {
            String msg = "Файл словаря не найден: " + fileName;
            if (log != null) log.println(msg);
            throw new DictionaryLoadException(msg);
        }

        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String n = WordleDictionary.normalize(line);
                if (n.length() == 5) {
                    words.add(n);
                }
            }

            if (words.isEmpty()) {
                String msg = "После фильтрации словарь пуст.";
                if (log != null) log.println(msg);
                throw new DictionaryLoadException(msg);
            }

            if (log != null) log.println("Словарь загружен. Всего слов: " + words.size());
            return new WordleDictionary(words, log);

        } catch (IOException e) {
            String msg = "Ошибка чтения файла словаря: " + e.getMessage();
            if (log != null) log.println(msg);
            throw new DictionaryLoadException(msg, e);
        }
    }
}
