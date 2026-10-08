package com.onebrain.game.puzzle;

/**
 * Localizable text identifiers. The model only stores keys + arguments; the UI
 * layer maps each key to a string resource so every language is supported
 * without the game logic knowing about Android resources.
 */
public enum PromptKey {
    MATH_FIND_MISSING,
    MATH_EVALUATE,
    MATH_SOLVE_X,
    MATH_LAST_LINE,

    LOGIC_SYMBOLS,
    LOGIC_FASTEST,
    LOGIC_SLOWEST,
    LOGIC_SECOND_FASTEST,
    LOGIC_SECOND_SLOWEST,
    LOGIC_STMT_FASTER,
    LOGIC_STMT_SLOWER,

    MEMORY_MEMORIZE,
    MEMORY_RECALL,

    PATTERN_FIND_MISSING,

    SEQUENCE_NEXT,

    VISUAL_FIND_ODD,

    SPATIAL_ROTATE_CW,
    SPATIAL_ROTATE_CCW,
    SPATIAL_ROTATE_180,
    SPATIAL_MIRROR_H,
    SPATIAL_MIRROR_V,
    SPATIAL_ROTATE_MIRROR,
    SPATIAL_ROTATE_MIRROR_V,

    WORD_ANAGRAM
}
