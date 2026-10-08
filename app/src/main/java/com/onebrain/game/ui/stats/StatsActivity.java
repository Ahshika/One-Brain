package com.onebrain.game.ui.stats;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.onebrain.game.R;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.databinding.ActivityScrollBinding;
import com.onebrain.game.databinding.ItemCategoryStatBinding;
import com.onebrain.game.player.CategoryStats;
import com.onebrain.game.player.Player;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Sections;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.util.TimeFormat;

/** Progression overview: totals, streaks, best scores and per-category performance. */
public final class StatsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityScrollBinding binding = ActivityScrollBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);
        binding.toolbar.setTitle(R.string.stats_title);

        Player player = game().getPlayer();
        PlayerStats s = player.getStats();
        LinearLayout c = binding.content;
        String dash = getString(R.string.dash);
        long today = game().getDaily().today();

        Sections.statRow(c, UiText.number(s.getPuzzlesPlayed()), R.string.stats_played,
                UiText.number(s.getTotalSolved()), R.string.stats_solved);
        Sections.statRow(c, getString(R.string.result_percent, Math.round(s.getAccuracy() * 100)), R.string.stats_accuracy,
                s.getTotalSolved() > 0 ? TimeFormat.clock(s.getAverageSolveTimeMs()) : dash, R.string.stats_avg_time);
        Sections.statRow(c, "🔥 " + s.getDisplayStreak(today, player.getInventory()), R.string.stats_current_streak,
                String.valueOf(s.getLongestStreak()), R.string.stats_longest_streak);
        Sections.statRow(c, s.getFastestSolveMs() > 0 ? TimeFormat.precise(s.getFastestSolveMs()) : dash, R.string.stats_fastest,
                UiText.number(s.getThreeStarCount()), R.string.stats_three_stars);
        Sections.statRow(c, UiText.number(s.getDailySolved()), R.string.stats_daily_solved,
                UiText.number(s.getBestFlawlessRun()), R.string.stats_flawless);

        Sections.header(c, R.string.stats_best_scores);
        Sections.statRow(c, UiText.number(s.getBestScore(GameMode.DAILY)), R.string.mode_daily,
                UiText.number(s.getBestScore(GameMode.PRACTICE)), R.string.mode_practice);
        Sections.statRow(c, UiText.number(s.getBestScore(GameMode.ENDLESS)), R.string.mode_endless,
                UiText.number(s.getBestScore(GameMode.TIME_ATTACK)), R.string.mode_time_attack);
        Sections.statRow(c, UiText.number(s.getBestScore(GameMode.WEEKLY)), R.string.mode_weekly,
                UiText.number(s.getBestScore(GameMode.PACK)), R.string.mode_pack);

        Sections.header(c, R.string.mode_challenge);
        Sections.statRow(c, UiText.number(s.getChallengeLevelsCleared()), R.string.stats_path_levels,
                "⭐ " + UiText.number(s.getChallengeStars()), R.string.stats_path_stars);

        Sections.header(c, R.string.stats_runs);
        Sections.statRow(c, UiText.number(s.getBestEndlessRun()), R.string.stats_best_endless,
                UiText.number(s.getBestTimeAttackRun()), R.string.stats_best_time_attack);

        Sections.header(c, R.string.stats_category_performance);
        LayoutInflater inflater = getLayoutInflater();
        for (PuzzleCategory category : PuzzleCategory.values()) {
            CategoryStats cs = s.getCategoryStats(category);
            ItemCategoryStatBinding row = ItemCategoryStatBinding.inflate(inflater, c, false);
            int color = ContextCompat.getColor(this, UiText.categoryColor(category));
            row.textIcon.setText(UiText.categoryIcon(category));
            row.textIcon.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x33000000));
            row.textName.setText(UiText.categoryName(category));
            int accuracy = Math.round(cs.getAccuracy() * 100);
            row.progress.setProgress(accuracy);
            row.progress.setIndicatorColor(color);
            row.textDetail.setText(getString(R.string.stats_category_line, cs.getSolved(), accuracy,
                    cs.getSolved() > 0 ? TimeFormat.clock(cs.getAverageSolveTimeMs()) : dash));
            c.addView(row.getRoot());
        }
    }
}
