package com.onebrain.game.ui.path;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.onebrain.game.R;
import com.onebrain.game.core.ChallengeLevel;
import com.onebrain.game.core.ChallengePath;
import com.onebrain.game.databinding.ActivityPathBinding;
import com.onebrain.game.databinding.ItemStageBinding;
import com.onebrain.game.player.ChallengeProgress;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.util.TimeFormat;

/**
 * Stage map of the Challenge Path. Stages unlock one after another; inside a
 * stage, levels unlock in order and show the best stars earned.
 */
public final class ChallengePathActivity extends BaseActivity {

    private static final int LEVELS_PER_ROW = 5;

    private ActivityPathBinding binding;
    private ChallengePath path;
    private final StageAdapter adapter = new StageAdapter();
    private boolean scrolled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPathBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);
        path = game().getChallengePath();

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        binding.btnContinue.setOnClickListener(v -> {
            tapFeedback(v);
            showLevel(progress().getHighestUnlocked());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChallengeProgress p = progress();
        binding.textSummary.setText(getString(R.string.path_summary, p.getClearedCount(), p.getTotalStars()));
        binding.btnContinue.setText(getString(R.string.path_continue, p.getHighestUnlocked() + 1));
        adapter.notifyDataSetChanged();
        if (!scrolled) {
            scrolled = true;
            int stage = path.stageOf(p.getHighestUnlocked());
            ((LinearLayoutManager) binding.recycler.getLayoutManager()).scrollToPositionWithOffset(stage, 0);
        }
    }

    private ChallengeProgress progress() {
        return game().getPlayer().getStats().getChallenge();
    }

    private int visibleStages() {
        int current = path.stageOf(progress().getHighestUnlocked());
        return Math.max(ChallengePath.NAMED_STAGES, current + 2);
    }

    /** Rules of a level as readable lines. */
    private String rules(ChallengeLevel level, boolean multiline) {
        String sep = multiline ? "\n" : " · ";
        StringBuilder sb = new StringBuilder();
        sb.append(getString(UiText.difficultyName(level.getPeakDifficulty())));
        sb.append(sep).append(getResources().getQuantityString(R.plurals.path_rule_puzzles,
                level.getPuzzleCount(), level.getPuzzleCount()));
        sb.append(sep).append(getResources().getQuantityString(R.plurals.path_attempts,
                level.getAttemptsPerPuzzle(), level.getAttemptsPerPuzzle()));
        sb.append(sep).append(level.getTimeLimitMillis() > 0
                ? getString(R.string.path_rule_timer, TimeFormat.clock(level.getTimeLimitMillis()))
                : getString(R.string.path_rule_untimed));
        if (!level.isHintsAllowed()) {
            sb.append(sep).append(getString(R.string.path_rule_no_hints));
        }
        if (multiline && level.isBoss()) {
            sb.append("\n\n").append(getString(R.string.path_rule_boss));
        }
        return sb.toString();
    }

    /** Description of a self-generated stage's theme, or {@code null} for the named stages. */
    @Nullable
    private String themeText(int stage, String sep) {
        if (!path.isInfinityStage(stage)) {
            return null;
        }
        ChallengePath.StageTheme theme = path.theme(stage);
        StringBuilder sb = new StringBuilder();
        if (theme.getFocus().size() >= 2) {
            sb.append(getString(R.string.path_theme_focus,
                    UiText.categoryIcon(theme.getFocus().get(0)) + " " + getString(UiText.categoryName(theme.getFocus().get(0))),
                    UiText.categoryIcon(theme.getFocus().get(1)) + " " + getString(UiText.categoryName(theme.getFocus().get(1)))));
        } else {
            sb.append(getString(R.string.path_theme_mixed));
        }
        if (theme.getModifier() == ChallengePath.StageModifier.SPRINT) {
            sb.append(sep).append(getString(R.string.path_mod_sprint));
        } else if (theme.getModifier() == ChallengePath.StageModifier.MARATHON) {
            sb.append(sep).append(getString(R.string.path_mod_marathon));
        }
        return sb.toString();
    }

    private void showLevel(int index) {
        if (!progress().isUnlocked(index)) {
            Toast.makeText(this, R.string.path_level_locked, Toast.LENGTH_SHORT).show();
            return;
        }
        ChallengeLevel level = path.level(index);
        String title = (level.isBoss() ? "👑 " : "") + getString(R.string.path_level_title, level.getNumber())
                + "  " + UiText.stars(progress().getStars(index));
        String stage = getString(R.string.path_stage_title, level.getStage() + 1,
                UiText.stageName(this, level.getStage()));
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(stage + "\n\n"
                        + (themeText(level.getStage(), "\n") != null ? themeText(level.getStage(), "\n") + "\n\n" : "")
                        + rules(level, true))
                .setPositiveButton(R.string.path_play, (d, w) -> Navigator.play(this, SessionConfig.challenge(index)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private View levelTile(Context ctx, int index, ChallengeProgress p) {
        float density = getResources().getDisplayMetrics().density;
        MaterialCardView card = new MaterialCardView(ctx);
        card.setRadius(14 * density);
        card.setCardElevation(0f);
        card.setStrokeWidth((int) density);
        boolean unlocked = p.isUnlocked(index);
        boolean cleared = p.isCleared(index);
        boolean current = index == p.getHighestUnlocked();
        boolean boss = (index + 1) % ChallengePath.LEVELS_PER_STAGE == 0;
        int fill = current ? R.color.ob_primary : cleared ? R.color.ob_primary_soft : R.color.ob_surface_alt;
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, fill));
        card.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(ctx,
                boss ? R.color.ob_gold : R.color.ob_stroke)));

        TextView tv = new TextView(ctx);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setTextColor(ContextCompat.getColor(ctx, current ? R.color.ob_on_primary : R.color.ob_text));
        String number = (boss ? "👑" : "") + (index + 1);
        String sub = !unlocked ? "🔒" : cleared ? starsShort(p.getStars(index)) : "▶";
        tv.setText(number + "\n" + sub);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tv.setLineSpacing(0f, 1.1f);
        int pad = (int) (6 * density);
        tv.setPadding(0, pad, 0, pad);
        card.addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        card.setAlpha(unlocked ? 1f : 0.55f);
        card.setOnClickListener(v -> {
            tapFeedback(v);
            showLevel(index);
        });
        return card;
    }

    private static String starsShort(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append('★');
        }
        return sb.length() == 0 ? "✓" : sb.toString();
    }

    private final class StageAdapter extends RecyclerView.Adapter<StageAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemStageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int stage) {
            ItemStageBinding b = holder.binding;
            Context ctx = b.getRoot().getContext();
            ChallengeProgress p = progress();
            int first = path.firstLevelOf(stage);
            boolean stageUnlocked = p.isUnlocked(first);

            b.textIcon.setText(UiText.stageIcon(stage));
            b.textTitle.setText(getString(R.string.path_stage_title, stage + 1, UiText.stageName(ctx, stage)));
            String theme = themeText(stage, " · ");
            String rules = rules(path.level(first + 5), false);
            b.textRules.setText(theme != null ? theme + "\n" + rules : rules);
            int stars = 0;
            for (int i = 0; i < ChallengePath.LEVELS_PER_STAGE; i++) {
                stars += p.getStars(first + i);
            }
            b.textStars.setText(getString(R.string.path_stage_stars, stars, ChallengePath.LEVELS_PER_STAGE * 3));

            b.levels.removeAllViews();
            if (!stageUnlocked) {
                b.textLocked.setVisibility(View.VISIBLE);
                b.textLocked.setText("🔒 " + getString(R.string.path_stage_locked, stage));
                b.getRoot().setAlpha(0.7f);
                return;
            }
            b.textLocked.setVisibility(View.GONE);
            b.getRoot().setAlpha(1f);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            LinearLayout row = null;
            for (int i = 0; i < ChallengePath.LEVELS_PER_STAGE; i++) {
                if (i % LEVELS_PER_ROW == 0) {
                    row = new LinearLayout(ctx);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    b.levels.addView(row);
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lp.setMargins(margin, margin, margin, margin);
                row.addView(levelTile(ctx, first + i, p), lp);
            }
        }

        @Override
        public int getItemCount() {
            return visibleStages();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final ItemStageBinding binding;

            Holder(ItemStageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
