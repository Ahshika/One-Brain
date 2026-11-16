package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.WordPuzzle;
import com.onebrain.game.puzzle.WordTheme;

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
        // Only easier puzzles get the theme clue; from Hard up the player is on their own.
        WordTheme theme = difficulty.ordinal() <= Difficulty.MEDIUM.ordinal() ? bank.themeOf(answer) : null;
        return new WordPuzzle(difficulty, seed, answer, letters, accepted, theme);
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
