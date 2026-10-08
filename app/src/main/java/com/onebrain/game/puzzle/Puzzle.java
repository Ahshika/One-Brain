package com.onebrain.game.puzzle;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.puzzle.visual.Visual;

/**
 * Root of the puzzle hierarchy.
 *
 * <pre>
 * Puzzle
 * ├── ChoicePuzzle (abstract, multiple choice + hints)
 * │   ├── MathPuzzle
 * │   ├── LogicPuzzle
 * │   ├── PatternPuzzle
 * │   ├── SequencePuzzle
 * │   └── SpatialPuzzle
 * ├── MemoryPuzzle   (Memorizable)
 * ├── VisualPuzzle   (odd one out)
 * └── WordPuzzle     (anagram)
 * </pre>
 *
 * A puzzle is a pure, immutable-ish model object: it knows how to validate an
 * answer and how to grade itself, but nothing about rendering.
 */
public abstract class Puzzle {

    private final PuzzleCategory category;
    private final Difficulty difficulty;
    private final long seed;
    private final Prompt prompt;
    private final Visual visual;
    private int hintsUsed;

    protected Puzzle(PuzzleCategory category, Difficulty difficulty, long seed, Prompt prompt, Visual visual) {
        this.category = category;
        this.difficulty = difficulty;
        this.seed = seed;
        this.prompt = prompt;
        this.visual = visual;
    }

    public abstract InputType getInputType();

    /** Puzzle-specific validation; implemented by each subclass. */
    protected abstract boolean evaluate(Answer answer);

    /** Template method: validates and lets the subclass react to a wrong attempt. */
    public final boolean check(Answer answer) {
        if (answer == null) {
            return false;
        }
        boolean correct = evaluate(answer);
        if (!correct) {
            onWrongAnswer(answer);
        }
        return correct;
    }

    /** Hook for subclasses (e.g. disable a wrong option so hints skip it). */
    protected void onWrongAnswer(Answer answer) {
    }

    /** Target solving time; subclasses tune it to their nature. */
    public int getParSeconds() {
        return difficulty.getParSeconds();
    }

    protected final void registerHintUsed() {
        hintsUsed++;
    }

    public final int getHintsUsed() {
        return hintsUsed;
    }

    public final PuzzleCategory getCategory() {
        return category;
    }

    public final Difficulty getDifficulty() {
        return difficulty;
    }

    public final long getSeed() {
        return seed;
    }

    public final Prompt getPrompt() {
        return prompt;
    }

    /** May be {@code null} when the input itself is the visual (e.g. letter tiles). */
    public final Visual getVisual() {
        return visual;
    }

    public final String getId() {
        return category.name() + '-' + difficulty.name() + '-' + Long.toHexString(seed);
    }
}
