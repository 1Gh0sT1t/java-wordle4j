package ru.yandex.practicum;

import java.io.File;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    public static void main(String[] args) {
        // имя файла словаря можно менять здесь
        String dictionaryFileName = "words_ru.txt";
        File logFile = new File("log.txt");

        try (PrintWriter logWriter = new PrintWriter(logFile, Constants.CHARSET_UTF_8)) {
            logWriter.println("Wordle starting...");

            WordleDictionaryLoader dictionaryLoader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = dictionaryLoader.load(dictionaryFileName);

            if (dictionary.size() == 0) {
                logWriter.println("Словарь пуст после фильтрации. Завершаем.");
                System.err.println("Словарь пуст. Проверьте файл словаря.");
                return;
            }

            WordleGame game = new WordleGame(dictionary, logWriter);
            playGame(game, logWriter);

            logWriter.println("Game finished. Answer: " + game.getAnswer());
            System.out.println("Игра окончена. Загаданное слово: " + game.getAnswer());

        } catch (DictionaryLoadException loadException) {
            System.err.println("Ошибка при загрузке словаря: " + loadException.getMessage());
            loadException.printStackTrace(System.err);
        } catch (Exception generalException) {
            // глобальный catch: все служебные ошибки в лог
            try (PrintWriter emergencyLog = new PrintWriter(logFile, Constants.CHARSET_UTF_8)) {
                emergencyLog.println("Unhandled exception: " + generalException.getMessage());
                generalException.printStackTrace(emergencyLog);
            } catch (Exception logException) {
                System.err.println("Не удалось записать лог: " + logException.getMessage());
            }
            System.err.println("Произошла ошибка: " + generalException.getMessage());
            generalException.printStackTrace(System.err);
        }
    }

    private static void playGame(WordleGame game, PrintWriter logWriter) {
        Scanner inputScanner = new Scanner(System.in, Constants.CHARSET_UTF_8);
        System.out.println("Игра Wordle. Угадайте слово из 5 букв. У вас 6 попыток.");
        System.out.println("Введите слово или нажмите Enter для подсказки.");

        while (!game.isFinished()) {
            System.out.printf("Осталось попыток: %d. Введите слово: ", game.getRemainingSteps());
            String inputLine;

            try {
                inputLine = inputScanner.nextLine();
            } catch (NoSuchElementException scannerException) {
                logWriter.println("Ввод завершён извне: " + scannerException.getMessage());
                break;
            }

            if (inputLine == null) {
                break;
            }

            String userInput = inputLine.strip();

            if (userInput.isEmpty()) {
                handleSuggestion(game, logWriter);
                continue;
            }

            handleUserGuess(game, userInput, logWriter);
        }

        printGameResult(game, logWriter);
        inputScanner.close();
    }

    private static void handleSuggestion(WordleGame game, PrintWriter logWriter) {
        try {
            String suggestion = game.suggestWord();
            if (suggestion == null) {
                System.out.println("Нет подходящих слов для подсказки.");
                logWriter.println("Нет подходящих слов для подсказки.");
            } else {
                System.out.println("Подсказка: " + suggestion);
                logWriter.println("Выдана подсказка: " + suggestion);
            }
        } catch (Exception suggestionException) {
            System.out.println("Ошибка при создании подсказки: " + suggestionException.getMessage());
            logWriter.println("Ошибка при создании подсказки: " + suggestionException.getMessage());
        }
    }

    private static void handleUserGuess(WordleGame game, String userInput, PrintWriter logWriter) {
        try {
            String normalizedInput = WordleDictionary.normalize(userInput);
            String hint = game.makeMove(normalizedInput);

            System.out.println(normalizedInput);
            System.out.println(hint);
            logWriter.println("Ход: " + normalizedInput + " => " + hint);

        } catch (InvalidWordException invalidWordException) {
            System.out.println("Неверный ввод: " + invalidWordException.getMessage());
            logWriter.println("Неверный ввод: " + invalidWordException.getMessage());
        } catch (WordNotFoundInDictionary wordNotFoundException) {
            System.out.println("Слово не найдено в словаре: " + wordNotFoundException.getMessage());
            logWriter.println("Слово не найдено: " + wordNotFoundException.getMessage());
        } catch (RuntimeException runtimeException) {
            System.out.println("Внутренняя ошибка: " + runtimeException.getMessage());
            logWriter.println("Внутренняя ошибка: " + runtimeException.getMessage());
            runtimeException.printStackTrace(logWriter);
        }
    }

    private static void printGameResult(WordleGame game, PrintWriter logWriter) {
        if (game.isWon()) {
            System.out.println("Поздравляю — вы угадали слово!");
            logWriter.println("Пользователь выиграл.");
        } else if (game.getRemainingSteps() == 0 && !game.isWon()) {
            System.out.println("К сожалению, попытки закончились.");
            System.out.println("Загаданное слово: " + game.getAnswer());
            logWriter.println("Пользователь проиграл. Ответ: " + game.getAnswer());
        } else {
            logWriter.println("Игровой цикл завершён досрочно.");
        }
    }
}