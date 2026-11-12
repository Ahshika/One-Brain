package com.onebrain.game.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.puzzle.WordClue;
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
    public void everyWordHasAClue() {
        WordBank bank = new WordBank();
        for (int len = 4; len <= 8; len++) {
            for (String w : bank.wordsOfLength(len)) {
                WordClue clue = bank.clueOf(w);
                assertNotNull("missing clue for " + w, clue);
                assertFalse("empty emoji for " + w, clue.getEmoji().trim().isEmpty());
            }
        }
    }

    @Test
    public void wordPuzzlesCarryTheirClue() {
        WordPuzzleGenerator generator = new WordPuzzleGenerator(new WordBank());
        for (Difficulty d : Difficulty.values()) {
            for (long seed = 0; seed < 40; seed++) {
                WordPuzzle p = (WordPuzzle) generator.generate(d, seed);
                assertNotNull(p.getAnswer(), p.getClue());
            }
        }
    }
}
