package ru.yandex.practicum;

import java.util.List;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private final List<String> words;
    private final PrintWriter log;
    private final Random rnd = new Random();

    public WordleDictionary(List<String> words, PrintWriter log) {
        this.words = new ArrayList<>(words);
        this.log = log;
    }

    public int size() {
        return words.size();
    }

    public boolean contains(String word) {
        if (word == null) return false;
        return words.contains(normalize(word));
    }

    public String getRandomWord() {
        if (words.isEmpty()) return null;
        return words.get(rnd.nextInt(words.size()));
    }

    public List<String> getAllWords() {
        return Collections.unmodifiableList(words);
    }

    /**
     * Нормализация: trim, toLowerCase, replace ё->е.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase();
        s = s.replace('ё', 'е');
        // если есть пробелы внутри, убрать их
        s = s.replaceAll("\\s+", "");
        return s;
    }

    //Фильтрует список кандидатов слов, которые соответствуют истории ходов.
    //historyGuesses - список введённых слов.
    //historyHints - соответствующие подсказки (строки из '+','^','-').

    public List<String> filterByHistory(List<String> historyGuesses, List<String> historyHints) {
        if (historyGuesses == null || historyHints == null || historyGuesses.size() != historyHints.size()) {
            throw new IllegalArgumentException("Некорректная история ходов.");
        }
        List<String> candidates = new ArrayList<>(words);

        for (int i = 0; i < historyGuesses.size(); i++) {
            String guess = historyGuesses.get(i);
            String hint = historyHints.get(i);
            candidates = candidates.stream()
                    .filter(word -> isConsistent(word, guess, hint))
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                if (log != null) log.println("В процессе фильтрации не осталось кандидатов после шага " + (i + 1));
                break;
            }
        }
        return candidates;
    }

    //Проверяет, соответствует ли candidate подсказке hint для угадываемого слова,
    //если был сделан ход guess.

    private boolean isConsistent(String candidate, String guess, String hint) {
        String expected = WordleGame.compareWords(candidate, guess);
        return expected.equals(hint);
    }
}
