package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.answer.TextAnswer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Unscramble letter tiles into a word. Any listed anagram is accepted. */
public final class WordPuzzle extends Puzzle implements Hintable {

    private final String answer;
    private final char[] letters;
    private final Set<String> accepted;
    private int revealedCount;

    public WordPuzzle(Difficulty difficulty, long seed, String answer, char[] scrambled, Set<String> accepted) {
        super(PuzzleCategory.WORD, difficulty, seed, new Prompt(PromptKey.WORD_ANAGRAM), null);
        this.answer = answer;
        this.letters = scrambled.clone();
        Set<String> all = new HashSet<>(accepted);
        all.add(answer);
        this.accepted = Collections.unmodifiableSet(all);
    }

    @Override
    public InputType getInputType() {
        return InputType.LETTER_TILES;
    }

    @Override
    protected boolean evaluate(Answer a) {
        return a instanceof TextAnswer && accepted.contains(((TextAnswer) a).getText());
    }

    @Override
    public int getParSeconds() {
        return answer.length() * 6;
    }

    @Override
    public boolean canUseHint() {
        return revealedCount < answer.length() - 2;
    }

    @Override
    public Hint useHint() {
        if (!canUseHint()) {
            return null;
        }
        registerHintUsed();
        return new Hint(Hint.Kind.REVEAL_LETTER, revealedCount++);
    }

    public char[] getLetters() {
        return letters.clone();
    }

    public int getLength() {
        return answer.length();
    }

    public char getAnswerLetterAt(int position) {
        return answer.charAt(position);
    }

    public int getRevealedCount() {
        return revealedCount;
    }

    /** Only used to reveal the solution after the final failed attempt. */
    public String getAnswer() {
        return answer;
    }

    public Set<String> getAcceptedAnswers() {
        return accepted;
    }
}
