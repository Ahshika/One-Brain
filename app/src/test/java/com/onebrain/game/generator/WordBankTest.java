package com.onebrain.game.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
