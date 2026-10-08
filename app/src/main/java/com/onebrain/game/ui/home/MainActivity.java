package com.onebrain.game.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;

import com.onebrain.game.R;
import com.onebrain.game.core.DailyPuzzleManager;
import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.databinding.ActivityMainBinding;
import com.onebrain.game.databinding.ViewModeTileBinding;
import com.onebrain.game.databinding.ViewSmallTileBinding;
import com.onebrain.game.monetization.BannerController;
import com.onebrain.game.player.Player;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.util.TimeFormat;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/** Home screen: today's puzzle, game modes and navigation. */
public final class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable countdown = new Runnable() {
        @Override
        public void run() {
            renderDailyStatus();
            handler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());

        setupTiles();
        binding.btnDaily.setOnClickListener(v -> {
            tapFeedback(v);
            Navigator.play(this, SessionConfig.daily(game().getDaily().today()));
        });
        binding.chipCoins.setOnClickListener(v -> Navigator.shop(this));

        game().getAds().gatherConsentAndInitialize(this);
        game().getBilling().connect();
        BannerController.attach(this, binding.bannerContainer, game().getAds());
    }

    private void setupTiles() {
        modeTile(binding.tilePath, "⛰️", R.string.mode_challenge, R.string.home_path_new,
                v -> Navigator.challengePath(this));
        modeTile(binding.tilePractice, "🎯", R.string.mode_practice, R.string.home_practice_sub,
                v -> Navigator.practice(this));
        modeTile(binding.tileEndless, "♾️", R.string.mode_endless, R.string.home_endless_sub,
                v -> Navigator.play(this, SessionConfig.endless()));
        modeTile(binding.tileTimeAttack, "⏱️", R.string.mode_time_attack, R.string.home_time_attack_sub,
                v -> Navigator.play(this, SessionConfig.timeAttack()));
        modeTile(binding.tileWeekly, "🏆", R.string.mode_weekly, R.string.home_weekly_sub,
                v -> Navigator.play(this, SessionConfig.weekly(game().getWeekly().currentWeek())));

        smallTile(binding.tileCategories, "🧩", R.string.home_categories, v -> Navigator.categories(this));
        smallTile(binding.tilePacks, "📦", R.string.home_packs, v -> Navigator.packs(this));
        smallTile(binding.tileShop, "🛒", R.string.home_shop, v -> Navigator.shop(this));
        smallTile(binding.tileStats, "📊", R.string.home_stats, v -> Navigator.stats(this));
        smallTile(binding.tileAchievements, "🏅", R.string.home_achievements, v -> Navigator.achievements(this));
        smallTile(binding.tileSettings, "⚙️", R.string.home_settings, v -> Navigator.settings(this));
    }

    private void modeTile(ViewModeTileBinding tile, String icon, int title, int subtitle, View.OnClickListener l) {
        tile.textIcon.setText(icon);
        tile.textTitle.setText(title);
        tile.textSubtitle.setText(subtitle);
        tile.getRoot().setOnClickListener(v -> {
            tapFeedback(v);
            l.onClick(v);
        });
    }

    private void smallTile(ViewSmallTileBinding tile, String icon, int title, View.OnClickListener l) {
        tile.textIcon.setText(icon);
        tile.textTitle.setText(title);
        tile.getRoot().setOnClickListener(v -> {
            tapFeedback(v);
            l.onClick(v);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        game().onAppResumed();
        render();
        handler.removeCallbacks(countdown);
        handler.post(countdown);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(countdown);
        super.onPause();
    }

    private void render() {
        Player player = game().getPlayer();
        DailyPuzzleManager daily = game().getDaily();
        long today = daily.today();
        int streak = player.getStats().getDisplayStreak(today, player.getInventory());
        binding.chipStreak.setText("🔥 " + streak);
        binding.chipStreak.setContentDescription(getString(R.string.home_streak_cd, streak));
        int coins = player.getWallet().getCoins();
        binding.chipCoins.setText("🪙 " + UiText.number(coins));
        binding.chipCoins.setContentDescription(getString(R.string.home_coins_cd, coins));

        PuzzleCategory category = daily.categoryFor(today);
        Difficulty difficulty = daily.difficultyFor(today);
        binding.textDailyNumber.setText(getString(R.string.home_daily_number, daily.todayNumber()));
        binding.textDailyIcon.setText(UiText.categoryIcon(category));
        binding.textDailyCategory.setText(UiText.categoryName(category));
        binding.textDailyDifficulty.setText(UiText.difficultyName(difficulty));
        renderDailyStatus();
        renderWeekStrip(player.getStats(), today);

        int next = player.getStats().getChallenge().getHighestUnlocked();
        binding.tilePath.textSubtitle.setText(next == 0 ? getString(R.string.home_path_new)
                : getString(R.string.home_path_sub, game().getChallengePath().stageOf(next) + 1, next + 1));

        int week = game().getWeekly().currentWeek();
        int weeklyBest = player.getStats().getWeeklyBestScore(week);
        binding.tileWeekly.textSubtitle.setText(weeklyBest > 0
                ? getString(R.string.home_weekly_best, UiText.number(weeklyBest))
                : getString(R.string.home_weekly_sub));
    }

    private void renderDailyStatus() {
        PlayerStats stats = game().getPlayer().getStats();
        long today = game().getDaily().today();
        Integer result = stats.getDailyResult(today);
        if (result == null && !stats.hasPlayedDaily(today)) {
            binding.textDailyStatus.setVisibility(View.GONE);
            binding.btnDaily.setVisibility(View.VISIBLE);
            binding.btnDaily.setText(stats.hasDailyInProgress(today)
                    ? R.string.home_continue_daily : R.string.home_play_daily);
            return;
        }
        binding.btnDaily.setVisibility(View.GONE);
        binding.textDailyStatus.setVisibility(View.VISIBLE);
        String head = result != null && result > 0
                ? getString(R.string.home_daily_solved) + "  " + UiText.stars(result)
                : getString(R.string.home_daily_failed);
        binding.textDailyStatus.setText(head + "\n" + getString(R.string.home_next_in,
                TimeFormat.countdown(game().getDaily().millisUntilNext())));
    }

    /** Seven dots for the last seven days: green = solved, red = missed attempt, faint = not played. */
    private void renderWeekStrip(PlayerStats stats, long today) {
        LinearLayout strip = binding.weekStrip;
        strip.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        for (int i = 6; i >= 0; i--) {
            long day = today - i;
            Integer r = stats.getDailyResult(day);
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);

            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.bg_dot);
            int color = r == null ? 0x40FFFFFF : r > 0 ? 0xFF4ADE80 : 0xFFFB7185;
            dot.setBackgroundTintList(ColorStateList.valueOf(color));
            int size = (int) (12 * density);
            if (i != 0) {
                dot.setAlpha(0.85f);
            }
            cell.addView(dot, new LinearLayout.LayoutParams(size, size));

            TextView label = new TextView(this);
            label.setText(LocalDate.ofEpochDay(day).getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.getDefault()));
            label.setTextColor(0xCCFFFFFF);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = (int) (4 * density);
            cell.addView(label, lp);

            strip.addView(cell, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
