package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.WordPuzzle;

import java.util.List;
import java.util.Random;
import java.util.Set;

/** Anagrams; word length grows with difficulty (4 → 7 letters). */
public final class WordPuzzleGenerator extends BasePuzzleGenerator {

    private final WordBank bank;

    public WordPuzzleGenerator(WordBank bank) {
        this.bank = bank;
    }

    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.WORD;
    }

    @Override
    public Puzzle generate(Difficulty difficulty, long seed) {
        Random r = new Random(seed);
        List<String> words = bank.wordsOfLength(4 + difficulty.ordinal());
        String answer = words.get(r.nextInt(words.size()));
        Set<String> accepted = bank.anagramsOf(answer);

        char[] letters = answer.toCharArray();
        for (int attempt = 0; attempt < 30; attempt++) {
            shuffle(letters, r);
            if (!accepted.contains(new String(letters))) {
                break;
            }
        }
        return new WordPuzzle(difficulty, seed, answer, letters, accepted);
    }

    private static void shuffle(char[] a, Random r) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            char t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }
}
