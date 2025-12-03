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
    private final Random random = new Random();

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.remainingSteps = Constants.MAX_ATTEMPTS;
        String selectedWord = dictionary.getRandomWord();

        if (selectedWord == null) {
            throw new EmptyDictionaryException("Словарь пуст, невозможен выбор ответа.");
        }
        this.answer = selectedWord;

        if (log != null) {
            log.println("Answer selected (hidden): " + answer);
        }
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
        if (historyGuesses.isEmpty()) {
            return false;
        }
        String lastGuess = historyGuesses.get(historyGuesses.size() - 1);
        return lastGuess.equals(answer);
    }

    //Делает ход: проверяет корректность, уменьшает счётчик и возвращает подсказку.
    //Бросает WordNotFoundInDictionary если слова нет в словаре,
    //InvalidWordException при неверной форме ввода.

    public String makeMove(String rawGuess) throws WordNotFoundInDictionary, InvalidWordException {
        if (rawGuess == null) {
            throw new InvalidWordException("Пустой ввод.");
        }
        String normalizedGuess = WordleDictionary.normalize(rawGuess);
        if (normalizedGuess.length() != 5) {
            throw new InvalidWordException("Слово должно содержать ровно 5 букв.");
        }
        if (!dictionary.contains(normalizedGuess)) {
            throw new WordNotFoundInDictionary("Слово не найдено в словаре: " + normalizedGuess);
        }

        // уменьшение попыток
        remainingSteps--;

        String hint = compareWords(answer, normalizedGuess);

        // сохраняем историю
        historyGuesses.add(normalizedGuess);
        historyHints.add(hint);

        if (log != null) {
            log.println("makeMove: guess=" + normalizedGuess + " hint=" + hint + " remaining=" + remainingSteps);
        }

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
            List<String> fallbackCandidates = dictionary.filterByHistory(historyGuesses, historyHints);
            for (String candidateWord : fallbackCandidates) {
                if (!historyGuesses.contains(candidateWord)) {
                    suggestionsGiven.add(candidateWord);
                    return candidateWord;
                }
            }
            return null;
        } else {
            String selectedSuggestion = candidates.get(random.nextInt(candidates.size()));
            suggestionsGiven.add(selectedSuggestion);
            if (log != null) {
                log.println("suggestWord => " + selectedSuggestion);
            }
            return selectedSuggestion;
        }
    }

    //Сравнение двух слов: secret и guess.
    //Возвращает строку длины 5 из символов '+', '^', '-'.
    //Алгоритм:
    //1) ставим '+' для точных вхождений и помечаем позиции использованные;
    //2) для остальных позиций считаем оставшиеся доступные буквы секретного слова (частоты),
    //если буква guess есть в оставшихся — '^' и уменьшаем счётчик, иначе '-'.

    public static String compareWords(String secretWord, String guessedWord) {
        if (secretWord == null || guessedWord == null) {
            throw new IllegalArgumentException("Null аргумент");
        }
        if (secretWord.length() != guessedWord.length()) {
            throw new IllegalArgumentException("Длина слов должна совпадать");
        }

        int wordLength = secretWord.length();
        char[] result = new char[wordLength];
        boolean[] matchedPositions = new boolean[wordLength];
        int[] letterFrequency = new int[Constants.RUSSIAN_ALPHABET_SIZE];

        for (int position = 0; position < wordLength; position++) {
            char secretChar = secretWord.charAt(position);
            char guessedChar = guessedWord.charAt(position);
            if (guessedChar == secretChar) {
                result[position] = '+';
                matchedPositions[position] = true;
            } else {
                result[position] = '?';
            }
        }

        for (int position = 0; position < wordLength; position++) {
            if (!matchedPositions[position]) {
                char secretChar = secretWord.charAt(position);
                int charIndex = charToIndex(secretChar);
                if (charIndex >= 0) {
                    letterFrequency[charIndex]++;
                }
            }
        }

        for (int position = 0; position < wordLength; position++) {
            if (result[position] == '?') {
                char guessedChar = guessedWord.charAt(position);
                int charIndex = charToIndex(guessedChar);
                if (charIndex >= 0 && letterFrequency[charIndex] > 0) {
                    result[position] = '^';
                    letterFrequency[charIndex]--;
                } else {
                    result[position] = '-';
                }
            }
        }

        return new String(result);
    }

    //Преобразование русской буквы в индекс 0..31 (а..я). Возвращает -1 если не алфавит.

    private static int charToIndex(char character) {
        // Нормализуем возможную 'ё' уже ранее заменили. Ожидаем 'а'..'я'
        if (character >= 'а' && character <= 'я') {
            return character - 'а';
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