package com.onebrain.game.generator;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Registry of {@link PuzzleGenerator}s keyed by category. Adding a new category
 * means writing one generator and registering it here; nothing else changes.
 */
public final class PuzzleFactory {

    private final Map<PuzzleCategory, PuzzleGenerator> generators = new EnumMap<>(PuzzleCategory.class);

    public PuzzleFactory() {
        register(new LogicPuzzleGenerator());
        register(new MathPuzzleGenerator());
        register(new MemoryPuzzleGenerator());
        register(new PatternPuzzleGenerator());
        register(new VisualPuzzleGenerator());
        register(new SequencePuzzleGenerator());
        register(new SpatialPuzzleGenerator());
        register(new WordPuzzleGenerator(new WordBank()));
    }

    public void register(PuzzleGenerator generator) {
        generators.put(generator.getCategory(), generator);
    }

    public Puzzle create(PuzzleCategory category, Difficulty difficulty, long seed) {
        PuzzleGenerator generator = generators.get(category);
        if (generator == null) {
            throw new IllegalArgumentException("No generator for " + category);
        }
        return generator.generate(difficulty, seed);
    }

    /** Picks a category deterministically from {@code random}, skipping excluded ones. */
    public PuzzleCategory randomCategory(Random random, Collection<PuzzleCategory> excluded) {
        List<PuzzleCategory> pool = new ArrayList<>();
        for (PuzzleCategory c : PuzzleCategory.values()) {
            if (generators.containsKey(c) && (excluded == null || !excluded.contains(c))) {
                pool.add(c);
            }
        }
        return pool.get(random.nextInt(pool.size()));
    }
}
