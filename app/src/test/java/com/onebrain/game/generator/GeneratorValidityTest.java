package com.onebrain.game.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.ChoicePuzzle;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.MemoryPuzzle;
import com.onebrain.game.puzzle.Option;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.VisualPuzzle;
import com.onebrain.game.puzzle.WordPuzzle;
import com.onebrain.game.puzzle.answer.CellsAnswer;
import com.onebrain.game.puzzle.answer.ChoiceAnswer;
import com.onebrain.game.puzzle.answer.TextAnswer;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** Fuzzes every generator: each puzzle must be well-formed and solvable. */
public class GeneratorValidityTest {

    private static final int SEEDS = 500;
    private final PuzzleFactory factory = new PuzzleFactory();

    @Test
    public void everyGeneratedPuzzleIsValid() {
        for (PuzzleCategory category : PuzzleCategory.values()) {
            for (Difficulty difficulty : Difficulty.values()) {
                for (long seed = 0; seed < SEEDS; seed++) {
                    Puzzle p = factory.create(category, difficulty, seed * 7919L + 13);
                    String id = p.getId();
                    assertEquals(id, category, p.getCategory());
                    assertEquals(id, difficulty, p.getDifficulty());
                    assertNotNull(id, p.getPrompt());
                    assertTrue(id, p.getParSeconds() > 0);
                    validate(p, id);
                }
            }
        }
    }

    private void validate(Puzzle p, String id) {
        if (p instanceof ChoicePuzzle) {
            ChoicePuzzle c = (ChoicePuzzle) p;
            List<Option> options = c.getOptions();
            assertTrue(id + " needs >= 3 options", options.size() >= 3);
            for (int i = 0; i < options.size(); i++) {
                for (int j = i + 1; j < options.size(); j++) {
                    assertFalse(id + " duplicate options", options.get(i).sameContent(options.get(j)));
                }
            }
            for (Option o : options) {
                if (!o.isGrid()) {
                    assertFalse(id + " negative option " + o.getText(), o.getText().startsWith("-"));
                }
            }
            int correct = c.getCorrectIndex();
            int wrong = correct == 0 ? 1 : 0;
            assertFalse(id, c.check(new ChoiceAnswer(wrong)));
            assertTrue(id, c.check(new ChoiceAnswer(correct)));
        } else if (p instanceof MemoryPuzzle) {
            MemoryPuzzle m = (MemoryPuzzle) p;
            assertTrue(id, m.getTargetCount() > 0 && m.getTargetCount() < m.getRows() * m.getCols());
            assertTrue(id, m.getMemorizeMillis() >= 1000);
            assertTrue(id, m.check(new CellsAnswer(m.getTargets())));
        } else if (p instanceof VisualPuzzle) {
            VisualPuzzle v = (VisualPuzzle) p;
            int odd = v.getOddIndex();
            int other = odd == 0 ? 1 : 0;
            if (v.getKind() == VisualPuzzle.Kind.COLOR) {
                assertTrue(id, v.getGrid().getColor(odd) != v.getGrid().getColor(other));
            } else {
                assertFalse(id, v.getGrid().getLabel(odd).equals(v.getGrid().getLabel(other)));
            }
            assertFalse(id, v.check(CellsAnswer.single(other)));
            assertTrue(id, v.check(CellsAnswer.single(odd)));
        } else if (p instanceof WordPuzzle) {
            WordPuzzle w = (WordPuzzle) p;
            char[] a = w.getAnswer().toCharArray();
            char[] l = w.getLetters();
            Arrays.sort(a);
            Arrays.sort(l);
            assertTrue(id + " letters must be a permutation", Arrays.equals(a, l));
            assertFalse(id + " must be scrambled", w.getAcceptedAnswers().contains(new String(w.getLetters())));
            assertTrue(id, w.check(new TextAnswer(w.getAnswer().toLowerCase())));
        } else {
            throw new AssertionError("Unexpected puzzle type " + p.getClass());
        }
    }

    @Test
    public void hintsNeverRevealEverything() {
        for (PuzzleCategory category : PuzzleCategory.values()) {
            Puzzle p = factory.create(category, Difficulty.HARD, 42L);
            com.onebrain.game.puzzle.Hintable h = (com.onebrain.game.puzzle.Hintable) p;
            int used = 0;
            while (h.canUseHint()) {
                Hint hint = h.useHint();
                assertNotNull(hint);
                used++;
                assertTrue("runaway hints", used < 20);
            }
            assertEquals(used, p.getHintsUsed());
            if (p instanceof ChoicePuzzle) {
                ChoicePuzzle c = (ChoicePuzzle) p;
                assertEquals(2, c.getOptions().size() - c.getDisabledOptions().size());
                assertFalse(c.getDisabledOptions().contains(c.getCorrectIndex()));
            }
        }
    }

    @Test
    public void generationIsDeterministic() {
        for (PuzzleCategory category : PuzzleCategory.values()) {
            for (Difficulty d : Difficulty.values()) {
                Puzzle a = factory.create(category, d, 123456789L);
                Puzzle b = factory.create(category, d, 123456789L);
                assertEquals(Fingerprint.of(a), Fingerprint.of(b));
            }
        }
    }
}
