package ru.yandex.practicum;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.util.List;

class WordleTest {

    private PrintWriter testLog;

    @BeforeEach
    void setUp() {
        // В тестах лог пишем в консоль
        testLog = new PrintWriter(System.out, true);
    }

    @Test
    void testNormalize() {
        String raw = "  Ёжик  ";
        String norm = WordleDictionary.normalize(raw);
        assertEquals("ежик", norm);

        String raw2 = "По ЁлКа";
        String norm2 = WordleDictionary.normalize(raw2);
        // пробелы внутри удаляются по реализации
        assertEquals("поелка", norm2);

        String raw3 = " ПрИвЕт ";
        assertEquals("привет", WordleDictionary.normalize(raw3));
    }

    @Test
    void testCompareWordsExample() {
        // пример из ТЗ: секрет "герой", догадка "гонец" -> "+^-^-"
        String secret = "герой";
        String guess = "гонец";
        String expected = "+^-^-";
        String actual = WordleGame.compareWords(secret, guess);
        assertEquals(expected, actual);
    }

    @Test
    void testCompareWordsWithRepeats() {
        // секрет "банан", догадка "анана" -> "^^^^-"
        String secret = "банан";
        String guess = "анана";
        String expected = "^^^^-";
        String actual = WordleGame.compareWords(secret, guess);
        assertEquals(expected, actual);
    }

    @Test
    void testFilterByHistoryKeepsSolution() {
        List<String> words = List.of("герой", "гонец", "реакт", "гонар");
        WordleDictionary dict = new WordleDictionary(words, testLog);

        // история: игрок вводил "гонец" и получил подсказку "+^-^-"
        List<String> historyGuesses = List.of("гонец");
        List<String> historyHints = List.of("+^-^-");

        List<String> filtered = dict.filterByHistory(historyGuesses, historyHints);
        // из набора кандидатов должен остаться "герой"
        assertTrue(filtered.contains("герой"));
        // и "гонец" не должен оставаться (совпадает с guess и не совпадает с hint)
        assertFalse(filtered.contains("гонец"));
    }

    @Test
    void testMakeMoveValidAndWin() throws Exception {
        // словарь с единственным словом — игра будет детерминированно угадываться
        WordleDictionary dict = new WordleDictionary(List.of("герой"), testLog);
        WordleGame game = new WordleGame(dict, testLog);

        // Сделаем ход с точным ответом
        String hint = game.makeMove("герой");
        assertEquals("+++++", hint);
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
    }

    @Test
    void testMakeMoveInvalidLengthThrows() {
        WordleDictionary dict = new WordleDictionary(List.of("герой"), testLog);
        WordleGame game = new WordleGame(dict, testLog);

        assertThrows(InvalidWordException.class, () -> game.makeMove("абв"));
    }

    @Test
    void testInvalidLengthThrows() {
        WordleDictionary dict = new WordleDictionary(List.of("герой"), testLog);
        WordleGame game = new WordleGame(dict, testLog);

        assertThrows(InvalidWordException.class, () -> game.makeMove("человек"));
    }

}
