package com.onebrain.game.core;

import com.onebrain.game.monetization.ProductCatalog;
import com.onebrain.game.player.Inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Catalogue of puzzle packs and their unlock rules. */
public final class PackManager {

    public static final String STARTER = "starter";
    public static final String MATH_MASTER = "math_master";
    public static final String MEMORY_MARATHON = "memory_marathon";
    public static final String WORD_WIZARD = "word_wizard";
    public static final String GRANDMASTER = "grandmaster";

    private final List<PuzzlePack> packs = Collections.unmodifiableList(Arrays.asList(
            new PuzzlePack(STARTER, "🌱", null, 0, false, false, 20),
            new PuzzlePack(MATH_MASTER, "🧮", ProductCatalog.PACK_MATH_MASTER, 3000, false, false, 30,
                    PuzzleCategory.MATH, PuzzleCategory.SEQUENCE, PuzzleCategory.PATTERN),
            new PuzzlePack(MEMORY_MARATHON, "🧠", ProductCatalog.PACK_MEMORY_MARATHON, 3000, false, false, 30,
                    PuzzleCategory.MEMORY, PuzzleCategory.VISUAL, PuzzleCategory.SPATIAL),
            new PuzzlePack(WORD_WIZARD, "📚", ProductCatalog.PACK_WORD_WIZARD, 3000, false, false, 30,
                    PuzzleCategory.WORD, PuzzleCategory.LOGIC),
            new PuzzlePack(GRANDMASTER, "👑", null, 0, true, true, 40)
    ));

    public List<PuzzlePack> getPacks() {
        return packs;
    }

    public PuzzlePack find(String id) {
        for (PuzzlePack p : packs) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public PuzzlePack findByProduct(String productId) {
        for (PuzzlePack p : packs) {
            if (productId.equals(p.getProductId())) {
                return p;
            }
        }
        return null;
    }

    public boolean isUnlocked(PuzzlePack pack, Inventory inventory) {
        if (pack.isFree() || inventory.ownsPack(pack.getId())) {
            return true;
        }
        return pack.isPremiumOnly() && inventory.isPremium();
    }

    public boolean isCompleted(PuzzlePack pack, Inventory inventory) {
        return inventory.getPackProgress(pack.getId()) >= pack.getLevels();
    }

    /** Next level to play (0-based), clamped to the last level once completed. */
    public int nextLevel(PuzzlePack pack, Inventory inventory) {
        return Math.min(pack.getLevels() - 1, inventory.getPackProgress(pack.getId()));
    }
}
