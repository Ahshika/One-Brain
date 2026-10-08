package com.onebrain.game.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onebrain.game.R;
import com.onebrain.game.achievement.Achievement;
import com.onebrain.game.achievement.AchievementManager;
import com.onebrain.game.databinding.ActivityListBinding;
import com.onebrain.game.databinding.ItemAchievementBinding;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.UiText;

import java.util.List;

public final class AchievementsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityListBinding binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);
        binding.toolbar.setTitle(R.string.achievements_title);

        AchievementManager manager = game().getAchievementManager();
        binding.textSummary.setVisibility(android.view.View.VISIBLE);
        binding.textSummary.setText(getString(R.string.achievements_summary,
                manager.getUnlockedCount(), manager.getAll().size()));

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(new Adapter(manager.getAll(), manager, game().getPlayer().getStats()));
    }

    private final class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        private final List<Achievement> items;
        private final AchievementManager manager;
        private final PlayerStats stats;

        Adapter(List<Achievement> items, AchievementManager manager, PlayerStats stats) {
            this.items = items;
            this.manager = manager;
            this.stats = stats;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemAchievementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Achievement a = items.get(position);
            ItemAchievementBinding b = holder.binding;
            boolean unlocked = manager.isUnlocked(a.getId());
            int progress = unlocked ? a.getTarget() : a.getProgress(stats);
            b.textIcon.setText(a.getIcon());
            b.textTitle.setText(UiText.achievementTitle(a.getId()));
            b.textDesc.setText(UiText.achievementDescription(a.getId()));
            b.textReward.setText(getString(R.string.achievements_reward, a.getRewardCoins()) + " 🪙");
            b.textProgress.setText(unlocked ? "✅" : getString(R.string.achievements_progress, progress, a.getTarget()));
            b.progress.setProgress(Math.round(progress * 100f / a.getTarget()));
            b.getRoot().setAlpha(unlocked ? 1f : 0.75f);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final ItemAchievementBinding binding;

            Holder(ItemAchievementBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
