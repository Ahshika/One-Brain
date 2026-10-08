package com.onebrain.game.monetization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Google Play in-app product ids. Create products with exactly these ids in the
 * Play Console (Monetize → Products → In-app products).
 */
public final class ProductCatalog {

    public static final String REMOVE_ADS = "remove_ads";
    public static final String PREMIUM = "premium_pack";
    public static final String COINS_SMALL = "coins_500";
    public static final String COINS_MEDIUM = "coins_1500";
    public static final String COINS_LARGE = "coins_4000";
    public static final String PACK_MATH_MASTER = "pack_math_master";
    public static final String PACK_MEMORY_MARATHON = "pack_memory_marathon";
    public static final String PACK_WORD_WIZARD = "pack_word_wizard";

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            REMOVE_ADS, PREMIUM, COINS_SMALL, COINS_MEDIUM, COINS_LARGE,
            PACK_MATH_MASTER, PACK_MEMORY_MARATHON, PACK_WORD_WIZARD));

    private ProductCatalog() {
    }

    public static boolean isConsumable(String productId) {
        return coinAmount(productId) > 0;
    }

    public static int coinAmount(String productId) {
        switch (productId) {
            case COINS_SMALL:
                return 500;
            case COINS_MEDIUM:
                return 1500;
            case COINS_LARGE:
                return 4000;
            default:
                return 0;
        }
    }
}
