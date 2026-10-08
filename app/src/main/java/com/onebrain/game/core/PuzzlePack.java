package com.onebrain.game.core;

import com.onebrain.game.util.SeedUtil;

/**
 * A fixed, ordered set of levels. Levels are generated from a seed so packs
 * cost nothing in APK size, yet are identical for every player.
 */
public final class PuzzlePack {

    private final String id;
    private final String icon;
    private final String productId;
    private final int coinPrice;
    private final boolean premiumOnly;
    private final boolean expertOnly;
    private final int levels;
    private final PuzzleCategory[] categories;

    public PuzzlePack(String id, String icon, String productId, int coinPrice, boolean premiumOnly,
                      boolean expertOnly, int levels, PuzzleCategory... categories) {
        this.id = id;
        this.icon = icon;
        this.productId = productId;
        this.coinPrice = coinPrice;
        this.premiumOnly = premiumOnly;
        this.expertOnly = expertOnly;
        this.levels = levels;
        this.categories = categories.length == 0 ? PuzzleCategory.values() : categories.clone();
    }

    public boolean isFree() {
        return productId == null && !premiumOnly && coinPrice == 0;
    }

    public PuzzleCategory categoryForLevel(int level) {
        return categories[Math.floorMod(level, categories.length)];
    }

    /** Difficulty ramps evenly through the pack: first quarter Easy … last quarter Expert. */
    public Difficulty difficultyForLevel(int level) {
        if (expertOnly) {
            return Difficulty.EXPERT;
        }
        int tiers = Difficulty.EXPERT.ordinal() + 1; // packs top out at Expert
        return Difficulty.fromIndex(level * tiers / Math.max(1, levels));
    }

    public long seedForLevel(int level) {
        return SeedUtil.mix(id.hashCode(), level);
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    /** Play Billing product id, or {@code null} if not sold for money. */
    public String getProductId() {
        return productId;
    }

    /** Coin price, or 0 if it cannot be unlocked with coins. */
    public int getCoinPrice() {
        return coinPrice;
    }

    public boolean isPremiumOnly() {
        return premiumOnly;
    }

    public int getLevels() {
        return levels;
    }
}
