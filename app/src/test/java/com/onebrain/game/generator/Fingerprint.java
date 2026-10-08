package com.onebrain.game.generator;

import com.onebrain.game.puzzle.ChoicePuzzle;
import com.onebrain.game.puzzle.MemoryPuzzle;
import com.onebrain.game.puzzle.Option;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.VisualPuzzle;
import com.onebrain.game.puzzle.WordPuzzle;
import com.onebrain.game.puzzle.visual.GridVisual;
import com.onebrain.game.puzzle.visual.LinesVisual;
import com.onebrain.game.puzzle.visual.TextVisual;
import com.onebrain.game.puzzle.visual.Visual;
import com.onebrain.game.puzzle.visual.VisualVisitor;

import java.util.Arrays;

/** Test helper: a string capturing everything a player sees in a puzzle. */
public final class Fingerprint {

    private Fingerprint() {
    }

    public static String of(Puzzle p) {
        StringBuilder sb = new StringBuilder(p.getId()).append('|').append(prompt(p.getPrompt()));
        if (p.getVisual() != null) {
            sb.append('|').append(visual(p.getVisual()));
        }
        if (p instanceof ChoicePuzzle) {
            for (Option o : ((ChoicePuzzle) p).getOptions()) {
                sb.append('|').append(o.isGrid() ? grid(o.getGrid()) : o.getText());
            }
            sb.append("|c=").append(((ChoicePuzzle) p).getCorrectIndex());
        } else if (p instanceof MemoryPuzzle) {
            sb.append("|t=").append(((MemoryPuzzle) p).getTargets());
        } else if (p instanceof VisualPuzzle) {
            sb.append("|o=").append(((VisualPuzzle) p).getOddIndex());
        } else if (p instanceof WordPuzzle) {
            sb.append("|w=").append(new String(((WordPuzzle) p).getLetters()));
        }
        return sb.toString();
    }

    private static String prompt(Prompt p) {
        return p.getKey() + Arrays.toString(p.getArgs());
    }

    private static String visual(Visual v) {
        return v.accept(new VisualVisitor<String>() {
            @Override
            public String visitText(TextVisual visual) {
                return visual.getText();
            }

            @Override
            public String visitLines(LinesVisual visual) {
                StringBuilder sb = new StringBuilder();
                for (Prompt p : visual.getLines()) {
                    sb.append(prompt(p)).append(';');
                }
                return sb.toString();
            }

            @Override
            public String visitGrid(GridVisual visual) {
                return grid(visual);
            }
        });
    }

    private static String grid(GridVisual g) {
        StringBuilder sb = new StringBuilder(g.getRows() + "x" + g.getCols() + ":");
        for (int i = 0; i < g.size(); i++) {
            sb.append(g.getLabel(i)).append('/').append(Integer.toHexString(g.getColor(i))).append(',');
        }
        return sb.toString();
    }
}
