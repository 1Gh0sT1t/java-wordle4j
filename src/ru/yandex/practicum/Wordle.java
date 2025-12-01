package ru.yandex.practicum;

import java.io.File;
import java.io.FileNotFoundException;
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
        String dictionaryFile = "words_ru.txt";
        File logFile = new File("log.txt");

        try (PrintWriter log = new PrintWriter(logFile, "UTF-8")) {
            log.println("Wordle starting...");

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.load(dictionaryFile);

            if (dictionary.size() == 0) {
                log.println("Словарь пуст после фильтрации. Завершаем.");
                System.err.println("Словарь пуст. Проверьте файл словаря.");
                return;
            }

            WordleGame game = new WordleGame(dictionary, log);
            playGame(game, log);

            log.println("Game finished. Answer: " + game.getAnswer());
            System.out.println("Игра окончена. Загаданное слово: " + game.getAnswer());

        } catch (DictionaryLoadException e) {
            System.err.println("Ошибка при загрузке словаря: " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (Exception e) {
            // глобальный catch: все служебные ошибки в лог
            try (PrintWriter log = new PrintWriter(logFile, "UTF-8")) {
                log.println("Unhandled exception: " + e.getMessage());
                e.printStackTrace(log);
            } catch (Exception ex) {
                System.err.println("Не удалось записать лог: " + ex.getMessage());
            }
            System.err.println("Произошла ошибка: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void playGame(WordleGame game, PrintWriter log) {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("Игра Wordle. Угадайте слово из 5 букв. У вас 6 попыток.");
        System.out.println("Введите слово или нажмите Enter для подсказки.");

        while (!game.isFinished()) {
            System.out.printf("Осталось попыток: %d. Введите слово: ", game.getRemainingSteps());
            String line;
            try {
                line = scanner.nextLine();
            } catch (NoSuchElementException e) {
                log.println("Ввод завершён извне: " + e.getMessage());
                break;
            }

            if (line == null) {
                break;
            }

            String input = line.strip();

            if (input.isEmpty()) {
                // подсказка пользователю
                try {
                    String suggestion = game.suggestWord();
                    if (suggestion == null) {
                        System.out.println("Нет подходящих слов для подсказки.");
                        log.println("Нет подходящих слов для подсказки.");
                    } else {
                        System.out.println("Подсказка: " + suggestion);
                        log.println("Выдана подсказка: " + suggestion);
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка при создании подсказки: " + e.getMessage());
                    log.println("Ошибка при создании подсказки: " + e.getMessage());
                }
                continue;
            }

            try {
                String normalized = WordleDictionary.normalize(input);
                String hint = game.makeMove(normalized);
                // выводим и логируем ход и подсказку
                System.out.println(normalized);
                System.out.println(hint);
                log.println("Ход: " + normalized + " => " + hint);

            } catch (InvalidWordException e) {
                System.out.println("Неверный ввод: " + e.getMessage());
                log.println("Неверный ввод: " + e.getMessage());
            } catch (WordNotFoundInDictionary e) {
                System.out.println("Слово не найдено в словаре: " + e.getMessage());
                log.println("Слово не найдено: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Внутренняя ошибка: " + e.getMessage());
                log.println("Внутренняя ошибка: " + e.getMessage());
                e.printStackTrace(log);
            }
        }

        if (game.isWon()) {
            System.out.println("Поздравляю — вы угадали слово!");
            log.println("Пользователь выиграл.");
        } else if (game.getRemainingSteps() == 0 && !game.isWon()) {
            System.out.println("К сожалению, попытки закончились.");
            System.out.println("Загаданное слово: " + game.getAnswer());
            log.println("Пользователь проиграл. Ответ: " + game.getAnswer());
        } else {
            log.println("Игровой цикл завершён досрочно.");
        }

        scanner.close();
    }
}
