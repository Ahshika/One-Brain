package com.onebrain.game.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.puzzle.WordPuzzle;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordBankTest {

    @Test
    public void listsHaveCorrectLengthsAndNoDuplicates() {
        WordBank bank = new WordBank();
        for (int len = 4; len <= 8; len++) {
            List<String> words = bank.wordsOfLength(len);
            assertTrue("need variety for length " + len, words.size() >= 50);
            Set<String> unique = new HashSet<>(words);
            assertEquals("duplicates in length " + len, words.size(), unique.size());
            for (String w : words) {
                assertEquals(w, len, w.length());
                assertTrue(w, w.matches("[A-Z]+"));
            }
        }
    }

    @Test
    public void everyWordHasATheme() {
        WordBank bank = new WordBank();
        for (int len = 4; len <= 8; len++) {
            for (String w : bank.wordsOfLength(len)) {
                assertNotNull("missing theme for " + w, bank.themeOf(w));
            }
        }
    }

    @Test
    public void onlyEasyAndMediumWordPuzzlesShowTheTheme() {
        WordPuzzleGenerator generator = new WordPuzzleGenerator(new WordBank());
        for (Difficulty d : Difficulty.values()) {
            boolean expectClue = d.ordinal() <= Difficulty.MEDIUM.ordinal();
            for (long seed = 0; seed < 40; seed++) {
                WordPuzzle p = (WordPuzzle) generator.generate(d, seed);
                if (expectClue) {
                    assertNotNull(d + " " + p.getAnswer(), p.getTheme());
                } else {
                    assertNull(d + " " + p.getAnswer(), p.getTheme());
                }
            }
        }
    }
}
