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
    private final Random random = new Random();

    public WordleDictionary(List<String> words, PrintWriter log) {
        this.words = new ArrayList<>(words);
        this.log = log;
    }

    public int size() {
        return words.size();
    }

    public boolean contains(String word) {
        if (word == null) {
            return false;
        }
        return words.contains(normalize(word));
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            return null;
        }
        return words.get(random.nextInt(words.size()));
    }

    public List<String> getAllWords() {
        return Collections.unmodifiableList(words);
    }

    /**
     * Нормализация: trim, toLowerCase, replace ё->е.
     */
    public static String normalize(String rawInput) {
        if (rawInput == null) {
            return "";
        }
        String normalized = rawInput.trim().toLowerCase();
        normalized = normalized.replace('ё', 'е');
        // если есть пробелы внутри, убрать их
        normalized = normalized.replaceAll("\\s+", "");
        return normalized;
    }

    //Фильтрует список кандидатов слов, которые соответствуют истории ходов.
    //historyGuesses - список введённых слов.
    //historyHints - соответствующие подсказки (строки из '+','^','-').

    public List<String> filterByHistory(List<String> historyGuesses, List<String> historyHints) {
        if (historyGuesses == null || historyHints == null || historyGuesses.size() != historyHints.size()) {
            throw new IllegalArgumentException("Некорректная история ходов.");
        }
        List<String> candidates = new ArrayList<>(words);

        for (int moveIndex = 0; moveIndex < historyGuesses.size(); moveIndex++) {
            String guess = historyGuesses.get(moveIndex);
            String hint = historyHints.get(moveIndex);
            candidates = candidates.stream()
                    .filter(word -> isConsistent(word, guess, hint))
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                if (log != null) {
                    log.println("В процессе фильтрации не осталось кандидатов после шага " + (moveIndex + 1));
                }
                break;
            }
        }
        return candidates;
    }

    //Проверяет, соответствует ли candidate подсказке hint для угадываемого слова,
    //если был сделан ход guess.

    private boolean isConsistent(String candidateWord, String guessedWord, String hint) {
        String expectedHint = WordleGame.compareWords(candidateWord, guessedWord);
        return expectedHint.equals(hint);
    }
}