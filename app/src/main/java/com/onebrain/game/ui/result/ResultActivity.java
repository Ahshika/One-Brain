package com.onebrain.game.ui.result;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import com.onebrain.game.R;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.databinding.ActivityResultBinding;
import com.onebrain.game.databinding.ViewResultStatBinding;
import com.onebrain.game.achievement.Achievement;
import com.onebrain.game.monetization.BannerController;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.session.SessionResult;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Anim;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.util.TimeFormat;

/** Post-session summary: stars, score, rewards, sharing and what to do next. */
public final class ResultActivity extends BaseActivity {

    private static final String EXTRA_RESULT = "result";
    private static final String EXTRA_CONFIG = "config";
    private static final String STATE_DOUBLED = "doubled";

    private ActivityResultBinding binding;
    private SessionResult result;
    private SessionConfig config;
    private boolean coinsDoubled;
    private boolean leaving;
    private ValueAnimator scoreAnimator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable countdown = new Runnable() {
        @Override
        public void run() {
            binding.textNext.setText(getString(R.string.home_next_in,
                    TimeFormat.countdown(game().getDaily().millisUntilNext())));
            handler.postDelayed(this, 1000L);
        }
    };

    public static Intent intent(Context context, SessionResult result, SessionConfig config) {
        return new Intent(context, ResultActivity.class)
                .putExtra(EXTRA_RESULT, result)
                .putExtra(EXTRA_CONFIG, config);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());

        result = IntentCompat.getSerializableExtra(getIntent(), EXTRA_RESULT, SessionResult.class);
        config = IntentCompat.getSerializableExtra(getIntent(), EXTRA_CONFIG, SessionConfig.class);
        if (result == null || config == null) {
            finish();
            return;
        }
        coinsDoubled = savedInstanceState != null && savedInstanceState.getBoolean(STATE_DOUBLED);
        boolean firstShow = savedInstanceState == null;

        bindHeader();
        bindStars(firstShow);
        bindStats(firstShow);
        bindAchievements(firstShow);
        bindActions();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leave(null);
            }
        });
        BannerController.attach(this, binding.bannerContainer, game().getAds());

        if (firstShow) {
            sound(result.isSuccess() ? SoundManager.Sound.WIN : SoundManager.Sound.WRONG);
        }
    }

    private void bindHeader() {
        GameMode mode = result.getMode();
        boolean single = mode == GameMode.DAILY || mode == GameMode.PACK;
        String emoji;
        int title;
        if (mode == GameMode.CHALLENGE) {
            boolean passed = result.isSuccess();
            emoji = passed ? (result.isStageCleared() ? "🏆" : "🏁") : "💥";
            binding.textEmoji.setText(emoji);
            binding.textTitle.setText(passed
                    ? getString(R.string.result_level_cleared, result.getChallengeLevel() + 1)
                    : getString(R.string.result_level_failed));
            binding.textSubtitle.setText(getString(R.string.path_stage_title, result.getChallengeStage() + 1,
                    UiText.stageName(this, result.getChallengeStage())));
            if (result.getClearBonus() > 0) {
                binding.textStreak.setVisibility(View.VISIBLE);
                binding.textStreak.setText(result.isStageCleared()
                        ? getString(R.string.result_stage_cleared, result.getClearBonus())
                        : getString(R.string.result_clear_bonus, result.getClearBonus()));
            }
            binding.textNewBest.setVisibility(View.GONE);
            return;
        }
        if (single) {
            emoji = result.isSuccess() ? (result.getStars() == 3 ? "🎉" : "✅") : "🤔";
            title = result.isSuccess() ? (result.getStars() == 3 ? R.string.result_brilliant : R.string.result_solved)
                    : R.string.result_failed;
        } else if (mode == GameMode.TIME_ATTACK) {
            emoji = "⏱️";
            title = R.string.result_time_up;
        } else if (mode == GameMode.ENDLESS) {
            emoji = "♾️";
            title = R.string.result_game_over;
        } else {
            emoji = result.getAccuracyPercent() >= 80 ? "🎉" : "💪";
            title = R.string.result_session_over;
        }
        binding.textEmoji.setText(emoji);
        binding.textTitle.setText(title);

        String modeName = getString(UiText.modeName(mode));
        if (result.getCategory() != null) {
            binding.textSubtitle.setText(getString(R.string.result_subtitle, modeName,
                    getString(UiText.categoryName(result.getCategory()))));
        } else {
            binding.textSubtitle.setText(modeName);
        }

        if (mode == GameMode.DAILY) {
            binding.textStreak.setVisibility(View.VISIBLE);
            binding.textStreak.setText("🔥 " + getResources().getQuantityString(
                    R.plurals.streak_days, result.getStreak(), result.getStreak()));
            binding.textNext.setVisibility(View.VISIBLE);
        }
        binding.textNewBest.setVisibility(result.isNewBest() ? View.VISIBLE : View.GONE);
    }

    private void bindStars(boolean animate) {
        ImageView[] stars = {binding.star1, binding.star2, binding.star3};
        int gold = ContextCompat.getColor(this, R.color.ob_gold);
        int empty = ContextCompat.getColor(this, R.color.ob_stroke);
        for (int i = 0; i < stars.length; i++) {
            boolean earned = i < result.getStars();
            stars[i].setImageTintList(ColorStateList.valueOf(earned ? gold : empty));
            if (animate && earned) {
                final int index = i;
                Anim.starIn(stars[i], 300L + i * 260L);
                handler.postDelayed(() -> game().getSound().play(SoundManager.Sound.STAR, 1f + index * 0.15f),
                        300L + i * 260L);
            }
        }
    }

    private void bindStats(boolean animate) {
        boolean single = result.getMode() == GameMode.DAILY || result.getMode() == GameMode.PACK;
        stat(binding.statA, R.string.result_score, UiText.number(result.getScore()));
        if (animate) {
            scoreAnimator = Anim.countUp(binding.statA.textValue, result.getScore(), 900L);
        }
        if (single) {
            stat(binding.statB, R.string.result_time, TimeFormat.clock(result.getTimeMs()));
        } else {
            stat(binding.statB, R.string.result_solved_label,
                    result.getSolved() + "/" + result.getPlayed());
        }
        int coins = coinsDoubled ? result.getCoins() * 2 : result.getCoins();
        stat(binding.statC, R.string.result_coins, "🪙 " + UiText.number(coins));
    }

    private void stat(ViewResultStatBinding view, int label, String value) {
        view.textLabel.setText(label);
        view.textValue.setText(value);
    }

    private void bindAchievements(boolean firstShow) {
        if (result.getAchievementIds().isEmpty()) {
            return;
        }
        binding.achievementsBox.setVisibility(View.VISIBLE);
        for (String id : result.getAchievementIds()) {
            Achievement a = game().getAchievementManager().find(id);
            TextView row = new TextView(this);
            row.setTextAppearance(R.style.TextAppearance_OneBrain_Title);
            String reward = a != null ? "  " + getString(R.string.achievements_reward, a.getRewardCoins()) + " 🪙" : "";
            row.setText((a != null ? a.getIcon() : "🏅") + "  " + getString(UiText.achievementTitle(id)) + reward);
            row.setPadding(0, 8, 0, 8);
            binding.achievementsList.addView(row);
        }
        if (firstShow) {
            handler.postDelayed(() -> sound(SoundManager.Sound.UNLOCK), 1200L);
        }
    }

    private void bindActions() {
        // Rewarded "double coins" — optional, once per result.
        if (result.getCoins() > 0 && !coinsDoubled && game().getAds().isRewardedReady()) {
            binding.btnDouble.setVisibility(View.VISIBLE);
            binding.btnDouble.setText("🎬 " + getString(R.string.result_double_coins, result.getCoins()));
            binding.btnDouble.setOnClickListener(v -> game().getAds().showRewarded(this, rewarded -> {
                if (rewarded && !coinsDoubled) {
                    coinsDoubled = true;
                    game().getPlayer().getWallet().add(result.getCoins());
                    game().persist();
                    binding.btnDouble.setEnabled(false);
                    binding.btnDouble.setText(R.string.result_coins_doubled);
                    bindStats(false);
                    sound(SoundManager.Sound.UNLOCK);
                }
            }));
        }

        binding.btnShare.setOnClickListener(v -> share());
        binding.btnHome.setOnClickListener(v -> leave(null));

        switch (result.getMode()) {
            case DAILY:
                binding.btnPrimary.setText(R.string.result_practice_more);
                binding.btnPrimary.setOnClickListener(v -> leave(() -> Navigator.practice(this)));
                break;
            case PACK:
                if (result.packHasNext()) {
                    binding.btnPrimary.setText(R.string.result_next_level);
                    binding.btnPrimary.setOnClickListener(v -> leave(() -> Navigator.play(this,
                            SessionConfig.pack(result.getPackId(), result.getPackLevel() + 1))));
                } else {
                    binding.btnPrimary.setText(R.string.result_retry);
                    binding.btnPrimary.setOnClickListener(v -> leave(() -> Navigator.play(this, config.replay())));
                }
                break;
            case CHALLENGE:
                if (result.isSuccess()) {
                    binding.btnPrimary.setText(R.string.result_next_level);
                    binding.btnPrimary.setOnClickListener(v -> leave(() -> Navigator.play(this,
                            SessionConfig.challenge(result.getChallengeLevel() + 1))));
                } else {
                    binding.btnPrimary.setText(R.string.result_retry);
                    binding.btnPrimary.setOnClickListener(v -> leave(() -> Navigator.play(this, config.replay())));
                }
                binding.btnHome.setText(R.string.result_levels);
                binding.btnHome.setOnClickListener(v -> leave(() -> Navigator.challengePath(this)));
                break;
            default:
                binding.btnPrimary.setText(R.string.result_play_again);
                binding.btnPrimary.setOnClickListener(v -> leave(() -> Navigator.play(this, config.replay())));
                break;
        }
    }

    private void share() {
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, ShareTextBuilder.build(this, result));
        try {
            startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.share_chooser, Toast.LENGTH_SHORT).show();
        }
    }

    /** Leaves the screen, possibly through a (rate-limited) interstitial. */
    private void leave(@Nullable Runnable next) {
        if (leaving) {
            return;
        }
        leaving = true;
        game().getAds().maybeShowInterstitial(this, () -> {
            if (next != null) {
                next.run();
            }
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_DOUBLED, coinsDoubled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (result != null && result.getMode() == GameMode.DAILY) {
            handler.post(countdown);
        }
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(countdown);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (scoreAnimator != null) {
            scoreAnimator.cancel();
        }
        super.onDestroy();
    }
}
