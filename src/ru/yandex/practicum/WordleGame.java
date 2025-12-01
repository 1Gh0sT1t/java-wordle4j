package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {

    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final String answer;
    private int remainingSteps;
    private final List<String> historyGuesses = new ArrayList<>();
    private final List<String> historyHints = new ArrayList<>();
    private final Set<String> suggestionsGiven = new HashSet<>();
    private final Random rnd = new Random();

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.remainingSteps = 6;
        String pick = dictionary.getRandomWord();
        if (pick == null) throw new RuntimeException("Словарь пуст, невозможен выбор ответа.");
        this.answer = pick;
        if (log != null) log.println("Answer selected (hidden): " + answer);
    }

    public String getAnswer() {
        return answer;
    }

    public int getRemainingSteps() {
        return remainingSteps;
    }

    public boolean isFinished() {
        return remainingSteps <= 0 || isWon();
    }

    public boolean isWon() {
        if (historyGuesses.isEmpty()) return false;
        String last = historyGuesses.get(historyGuesses.size() - 1);
        return last.equals(answer);
    }

    //Делает ход: проверяет корректность, уменьшает счётчик и возвращает подсказку.
    //Бросает WordNotFoundInDictionary если слова нет в словаре,
    //InvalidWordException при неверной форме ввода.

    public String makeMove(String rawGuess) throws WordNotFoundInDictionary, InvalidWordException {
        if (rawGuess == null) throw new InvalidWordException("Пустой ввод.");
        String guess = WordleDictionary.normalize(rawGuess);
        if (guess.length() != 5) {
            throw new InvalidWordException("Слово должно содержать ровно 5 букв.");
        }
        if (!dictionary.contains(guess)) {
            throw new WordNotFoundInDictionary("Слово не найдено в словаре: " + guess);
        }

        // уменьшение попыток
        remainingSteps--;

        String hint = compareWords(answer, guess);

        // сохраняем историю
        historyGuesses.add(guess);
        historyHints.add(hint);

        if (log != null) log.println("makeMove: guess=" + guess + " hint=" + hint + " remaining=" + remainingSteps);

        return hint;
    }

    //Возвращает подсказку-слово из списка подходящих слов, учитывая историю.
    //Ставит цель — не выдавать уже выданные подсказки.

    public String suggestWord() {
        List<String> candidates = dictionary.filterByHistory(historyGuesses, historyHints);
        // отсеять уже предлагаемые и уже введённые
        candidates.removeAll(historyGuesses);
        candidates.removeAll(suggestionsGiven.stream().toList());

        if (candidates.isEmpty()) {
            // возможно, стоит вернуть любой из оставшихся слов, чтобы помочь игроку
            List<String> fallback = dictionary.filterByHistory(historyGuesses, historyHints);
            for (String w : fallback) {
                if (!historyGuesses.contains(w)) {
                    suggestionsGiven.add(w);
                    return w;
                }
            }
            return null;
        } else {
            String pick = candidates.get(rnd.nextInt(candidates.size()));
            suggestionsGiven.add(pick);
            if (log != null) log.println("suggestWord => " + pick);
            return pick;
        }
    }

    //Сравнение двух слов: secret и guess.
    //Возвращает строку длины 5 из символов '+', '^', '-'.
    //Алгоритм:
    //1) ставим '+' для точных вхождений и помечаем позиции использованные;
    //2) для остальных позиций считаем оставшиеся доступные буквы секретного слова (частоты),
    //если буква guess есть в оставшихся — '^' и уменьшаем счётчик, иначе '-'.

    public static String compareWords(String secret, String guess) {
        if (secret == null || guess == null) throw new IllegalArgumentException("Null аргумент");
        if (secret.length() != guess.length()) throw new IllegalArgumentException("Длина слов должна совпадать");

        int n = secret.length();
        char[] result = new char[n];
        boolean[] matched = new boolean[n];
        int[] freq = new int[26];


        for (int i = 0; i < n; i++) {
            char sc = secret.charAt(i);
            char gc = guess.charAt(i);
            if (gc == sc) {
                result[i] = '+';
                matched[i] = true;
            } else {
                result[i] = '?';
            }
        }


        for (int i = 0; i < n; i++) {
            if (!matched[i]) {
                char sc = secret.charAt(i);
                int idx = charToIndex(sc);
                if (idx >= 0) freq[idx]++;
            }
        }


        for (int i = 0; i < n; i++) {
            if (result[i] == '?') {
                char gc = guess.charAt(i);
                int idx = charToIndex(gc);
                if (idx >= 0 && freq[idx] > 0) {
                    result[i] = '^';
                    freq[idx]--;
                } else {
                    result[i] = '-';
                }
            }
        }

        return new String(result);
    }

    //Преобразование русской буквы в индекс 0..31 (а..я). Возвращает -1 если не алфавит.

    private static int charToIndex(char c) {
        // Нормализуем возможную 'ё' уже ранее заменили. Ожидаем 'а'..'я'
        if (c >= 'а' && c <= 'я') {
            return c - 'а';
        } else {
            return -1;
        }
    }


    public List<String> getHistoryGuesses() {
        return List.copyOf(historyGuesses);
    }

    public List<String> getHistoryHints() {
        return List.copyOf(historyHints);
    }
}
