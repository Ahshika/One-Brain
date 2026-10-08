package com.onebrain.game.ui.categories;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.onebrain.game.R;
import com.onebrain.game.core.Difficulty;
import com.onebrain.game.core.DifficultyManager;
import com.onebrain.game.core.PuzzleCategory;
import com.onebrain.game.databinding.ActivityListBinding;
import com.onebrain.game.databinding.ItemCategoryBinding;
import com.onebrain.game.player.CategoryStats;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.common.UiText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Category browser. In practice mode it also offers a "Mixed" option; tapping a
 * category opens the difficulty picker and starts a practice session.
 */
public final class CategoriesActivity extends BaseActivity {

    private static final String EXTRA_PRACTICE = "practice";

    private ActivityListBinding binding;
    private Adapter adapter;

    public static Intent intent(Context context, boolean practice) {
        return new Intent(context, CategoriesActivity.class).putExtra(EXTRA_PRACTICE, practice);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);

        boolean practice = getIntent().getBooleanExtra(EXTRA_PRACTICE, false);
        binding.toolbar.setTitle(practice ? R.string.practice_title : R.string.categories_title);

        List<PuzzleCategory> items = new ArrayList<>();
        if (practice) {
            items.add(null); // "Mixed"
        }
        items.addAll(Arrays.asList(PuzzleCategory.values()));
        adapter = new Adapter(items);
        binding.recycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void onCategoryClicked(@Nullable PuzzleCategory category) {
        Difficulty recommended = category == null ? null
                : game().getDifficultyManager().recommend(game().getPlayer().getStats(), category);
        boolean premium = game().getPlayer().getInventory().isPremium();
        int pathLevel = game().getPlayer().getStats().getChallenge().getHighestUnlocked();
        DifficultyManager dm = game().getDifficultyManager();

        Difficulty[] levels = Difficulty.values();
        String[] labels = new String[levels.length + 1];
        labels[0] = recommended != null
                ? getString(R.string.difficulty_recommended, getString(UiText.difficultyName(recommended)))
                : getString(R.string.category_mixed);
        for (int i = 0; i < levels.length; i++) {
            String name = getString(UiText.difficultyName(levels[i]));
            labels[i + 1] = dm.isUnlockedForPractice(levels[i], premium, pathLevel)
                    ? name : getString(R.string.difficulty_locked, name);
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_difficulty)
                .setItems(labels, (d, which) -> {
                    Difficulty chosen = which == 0 ? null : levels[which - 1];
                    if (chosen != null && !dm.isUnlockedForPractice(chosen, premium, pathLevel)) {
                        showLockedDialog(chosen);
                        return;
                    }
                    Navigator.play(this, SessionConfig.practice(category, chosen));
                })
                .show();
    }

    private void showLockedDialog(Difficulty difficulty) {
        int needed = difficulty == Difficulty.EXPERT
                ? DifficultyManager.EXPERT_UNLOCK_LEVEL : DifficultyManager.MASTER_UNLOCK_LEVEL;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.difficulty_unlock_title)
                .setMessage(getString(R.string.difficulty_unlock_msg, needed,
                        getString(UiText.difficultyName(difficulty))))
                .setPositiveButton(R.string.mode_challenge, (d, w) -> Navigator.challengePath(this))
                .setNeutralButton(R.string.open_shop, (d, w) -> Navigator.shop(this))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private final class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        private final List<PuzzleCategory> items;

        Adapter(List<PuzzleCategory> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PuzzleCategory c = items.get(position);
            ItemCategoryBinding b = holder.binding;
            Context ctx = b.getRoot().getContext();
            if (c == null) {
                b.textIcon.setText("🎲");
                b.textIcon.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.ob_primary_soft)));
                b.textName.setText(R.string.category_mixed);
                b.textStats.setText(R.string.category_mixed_desc);
                b.textLevel.setVisibility(View.GONE);
            } else {
                PlayerStats stats = game().getPlayer().getStats();
                CategoryStats cs = stats.getCategoryStats(c);
                int color = ContextCompat.getColor(ctx, UiText.categoryColor(c));
                b.textIcon.setText(UiText.categoryIcon(c));
                b.textIcon.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x33000000));
                b.textName.setText(UiText.categoryName(c));
                b.textStats.setText(getString(R.string.category_stats_line, cs.getSolved(),
                        Math.round(cs.getAccuracy() * 100)));
                Difficulty level = game().getDifficultyManager().recommend(cs);
                b.textLevel.setVisibility(View.VISIBLE);
                b.textLevel.setText(getString(R.string.category_level, getString(UiText.difficultyName(level))));
                b.textLevel.setTextColor(ContextCompat.getColor(ctx, UiText.difficultyColor(level)));
            }
            b.getRoot().setOnClickListener(v -> {
                tapFeedback(v);
                onCategoryClicked(c);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final ItemCategoryBinding binding;

            Holder(ItemCategoryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
