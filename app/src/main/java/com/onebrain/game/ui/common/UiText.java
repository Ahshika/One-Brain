package com.onebrain.game.ui.common;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.onebrain.game.R;
import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.puzzle.Prompt;
import com.onebrain.game.puzzle.PromptKey;

import java.text.NumberFormat;

/**
 * Maps model identifiers (enums, prompt keys, ids) to localized resources.
 * Keeping this mapping in the UI layer is what lets the game logic stay free
 * of Android dependencies.
 */
public final class UiText {

    private UiText() {
    }

    public static String format(Context context, Prompt prompt) {
        return context.getString(promptRes(prompt.getKey()), prompt.getArgs());
    }

    @StringRes
    public static int promptRes(PromptKey key) {
        switch (key) {
            case MATH_FIND_MISSING:
                return R.string.prompt_math_missing;
            case MATH_EVALUATE:
                return R.string.prompt_math_evaluate;
            case MATH_SOLVE_X:
                return R.string.prompt_math_solve_x;
            case MATH_LAST_LINE:
                return R.string.prompt_math_last_line;
            case LOGIC_SYMBOLS:
                return R.string.prompt_logic_symbols;
            case LOGIC_FASTEST:
                return R.string.prompt_logic_fastest;
            case LOGIC_SLOWEST:
                return R.string.prompt_logic_slowest;
            case LOGIC_SECOND_FASTEST:
                return R.string.prompt_logic_second_fastest;
            case LOGIC_SECOND_SLOWEST:
                return R.string.prompt_logic_second_slowest;
            case LOGIC_STMT_FASTER:
                return R.string.stmt_faster;
            case LOGIC_STMT_SLOWER:
                return R.string.stmt_slower;
            case MEMORY_MEMORIZE:
                return R.string.prompt_memory_memorize;
            case MEMORY_RECALL:
                return R.string.prompt_memory_recall;
            case PATTERN_FIND_MISSING:
                return R.string.prompt_pattern_missing;
            case SEQUENCE_NEXT:
                return R.string.prompt_sequence_next;
            case VISUAL_FIND_ODD:
                return R.string.prompt_visual_odd;
            case SPATIAL_ROTATE_CW:
                return R.string.prompt_spatial_cw;
            case SPATIAL_ROTATE_CCW:
                return R.string.prompt_spatial_ccw;
            case SPATIAL_ROTATE_180:
                return R.string.prompt_spatial_180;
            case SPATIAL_MIRROR_H:
                return R.string.prompt_spatial_mirror_h;
            case SPATIAL_MIRROR_V:
                return R.string.prompt_spatial_mirror_v;
            case SPATIAL_ROTATE_MIRROR:
                return R.string.prompt_spatial_rotate_mirror;
            case SPATIAL_ROTATE_MIRROR_V:
                return R.string.prompt_spatial_rotate_mirror_v;
            case WORD_ANAGRAM:
            default:
                return R.string.prompt_word_anagram;
        }
    }

    @StringRes
    public static int categoryName(PuzzleCategory c) {
        switch (c) {
            case LOGIC:
                return R.string.category_logic;
            case MATH:
                return R.string.category_math;
            case MEMORY:
                return R.string.category_memory;
            case PATTERN:
                return R.string.category_pattern;
            case VISUAL:
                return R.string.category_visual;
            case SEQUENCE:
                return R.string.category_sequence;
            case SPATIAL:
                return R.string.category_spatial;
            case WORD:
            default:
                return R.string.category_word;
        }
    }

    public static String categoryIcon(PuzzleCategory c) {
        switch (c) {
            case LOGIC:
                return "🧩";
            case MATH:
                return "➕";
            case MEMORY:
                return "🧠";
            case PATTERN:
                return "🔷";
            case VISUAL:
                return "👁️";
            case SEQUENCE:
                return "🔢";
            case SPATIAL:
                return "🔄";
            case WORD:
            default:
                return "🔤";
        }
    }

    @ColorRes
    public static int categoryColor(PuzzleCategory c) {
        switch (c) {
            case LOGIC:
                return R.color.cat_logic;
            case MATH:
                return R.color.cat_math;
            case MEMORY:
                return R.color.cat_memory;
            case PATTERN:
                return R.color.cat_pattern;
            case VISUAL:
                return R.color.cat_visual;
            case SEQUENCE:
                return R.color.cat_sequence;
            case SPATIAL:
                return R.color.cat_spatial;
            case WORD:
            default:
                return R.color.cat_word;
        }
    }

    @StringRes
    public static int difficultyName(Difficulty d) {
        switch (d) {
            case EASY:
                return R.string.difficulty_easy;
            case MEDIUM:
                return R.string.difficulty_medium;
            case HARD:
                return R.string.difficulty_hard;
            case MASTER:
                return R.string.difficulty_master;
            case EXPERT:
            default:
                return R.string.difficulty_expert;
        }
    }

    @ColorRes
    public static int difficultyColor(Difficulty d) {
        switch (d) {
            case EASY:
                return R.color.diff_easy;
            case MEDIUM:
                return R.color.diff_medium;
            case HARD:
                return R.color.diff_hard;
            case MASTER:
                return R.color.diff_master;
            case EXPERT:
            default:
                return R.color.diff_expert;
        }
    }

    @StringRes
    public static int modeName(GameMode mode) {
        switch (mode) {
            case DAILY:
                return R.string.mode_daily;
            case PRACTICE:
                return R.string.mode_practice;
            case ENDLESS:
                return R.string.mode_endless;
            case TIME_ATTACK:
                return R.string.mode_time_attack;
            case WEEKLY:
                return R.string.mode_weekly;
            case CHALLENGE:
                return R.string.mode_challenge;
            case PACK:
            default:
                return R.string.mode_pack;
        }
    }

    @StringRes
    public static int achievementTitle(String id) {
        switch (id) {
            case "first_solve":
                return R.string.ach_first_solve_title;
            case "daily_7":
                return R.string.ach_daily_7_title;
            case "daily_30":
                return R.string.ach_daily_30_title;
            case "solved_30":
                return R.string.ach_solved_30_title;
            case "solved_100":
                return R.string.ach_solved_100_title;
            case "solved_500":
                return R.string.ach_solved_500_title;
            case "flawless_10":
                return R.string.ach_flawless_10_title;
            case "speed_10":
                return R.string.ach_speed_10_title;
            case "streak_7":
                return R.string.ach_streak_7_title;
            case "streak_30":
                return R.string.ach_streak_30_title;
            case "stars_25":
                return R.string.ach_stars_25_title;
            case "all_categories":
                return R.string.ach_all_categories_title;
            case "expert_10":
                return R.string.ach_expert_10_title;
            case "endless_20":
                return R.string.ach_endless_20_title;
            case "time_attack_15":
                return R.string.ach_time_attack_15_title;
            case "path_10":
                return R.string.ach_path_10_title;
            case "path_50":
                return R.string.ach_path_50_title;
            case "path_100":
                return R.string.ach_path_100_title;
            case "path_stars_150":
                return R.string.ach_path_stars_150_title;
            case "master_10":
                return R.string.ach_master_10_title;
            case "weekly_1":
            default:
                return R.string.ach_weekly_1_title;
        }
    }

    @StringRes
    public static int achievementDescription(String id) {
        switch (id) {
            case "first_solve":
                return R.string.ach_first_solve_desc;
            case "daily_7":
                return R.string.ach_daily_7_desc;
            case "daily_30":
                return R.string.ach_daily_30_desc;
            case "solved_30":
                return R.string.ach_solved_30_desc;
            case "solved_100":
                return R.string.ach_solved_100_desc;
            case "solved_500":
                return R.string.ach_solved_500_desc;
            case "flawless_10":
                return R.string.ach_flawless_10_desc;
            case "speed_10":
                return R.string.ach_speed_10_desc;
            case "streak_7":
                return R.string.ach_streak_7_desc;
            case "streak_30":
                return R.string.ach_streak_30_desc;
            case "stars_25":
                return R.string.ach_stars_25_desc;
            case "all_categories":
                return R.string.ach_all_categories_desc;
            case "expert_10":
                return R.string.ach_expert_10_desc;
            case "endless_20":
                return R.string.ach_endless_20_desc;
            case "time_attack_15":
                return R.string.ach_time_attack_15_desc;
            case "path_10":
                return R.string.ach_path_10_desc;
            case "path_50":
                return R.string.ach_path_50_desc;
            case "path_100":
                return R.string.ach_path_100_desc;
            case "path_stars_150":
                return R.string.ach_path_stars_150_desc;
            case "master_10":
                return R.string.ach_master_10_desc;
            case "weekly_1":
            default:
                return R.string.ach_weekly_1_desc;
        }
    }

    @StringRes
    public static int packName(String id) {
        switch (id) {
            case "math_master":
                return R.string.pack_math_master_name;
            case "memory_marathon":
                return R.string.pack_memory_marathon_name;
            case "word_wizard":
                return R.string.pack_word_wizard_name;
            case "grandmaster":
                return R.string.pack_grandmaster_name;
            case "starter":
            default:
                return R.string.pack_starter_name;
        }
    }

    @StringRes
    public static int packDescription(String id) {
        switch (id) {
            case "math_master":
                return R.string.pack_math_master_desc;
            case "memory_marathon":
                return R.string.pack_memory_marathon_desc;
            case "word_wizard":
                return R.string.pack_word_wizard_desc;
            case "grandmaster":
                return R.string.pack_grandmaster_desc;
            case "starter":
            default:
                return R.string.pack_starter_desc;
        }
    }

    private static final int[] STAGE_NAMES = {
            R.string.stage_1, R.string.stage_2, R.string.stage_3, R.string.stage_4, R.string.stage_5,
            R.string.stage_6, R.string.stage_7, R.string.stage_8, R.string.stage_9, R.string.stage_10
    };
    private static final String[] STAGE_ICONS = {
            "🌱", "📘", "💡", "♟️", "⚔️",
            "🎓", "🧠", "⚡", "🏆", "👑"
    };

    /** Name of a 0-based Challenge Path stage. */
    public static String stageName(Context context, int stage) {
        if (stage < STAGE_NAMES.length) {
            return context.getString(STAGE_NAMES[stage]);
        }
        return context.getString(R.string.stage_infinity, stage - STAGE_NAMES.length + 1);
    }

    public static String stageIcon(int stage) {
        return stage < STAGE_ICONS.length ? STAGE_ICONS[stage] : "♾️";
    }

    public static String number(long value) {
        return NumberFormat.getIntegerInstance().format(value);
    }

    public static String stars(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < stars ? "⭐" : "☆");
        }
        return sb.toString();
    }
}
