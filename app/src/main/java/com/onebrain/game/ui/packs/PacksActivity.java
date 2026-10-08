package com.onebrain.game.ui.packs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.onebrain.game.R;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.core.PackManager;
import com.onebrain.game.core.PuzzlePack;
import com.onebrain.game.databinding.ActivityListBinding;
import com.onebrain.game.databinding.ItemPackBinding;
import com.onebrain.game.monetization.BillingManager;
import com.onebrain.game.player.Inventory;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.ui.shop.PurchaseMessages;

import java.util.List;

/** Puzzle packs: play unlocked ones, buy others with real money or coins. */
public final class PacksActivity extends BaseActivity {

    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityListBinding binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);
        binding.toolbar.setTitle(R.string.packs_title);

        adapter = new Adapter(game().getPackManager().getPacks());
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        BillingManager billing = game().getBilling();
        billing.connect();
        billing.getProducts().observe(this, p -> adapter.notifyDataSetChanged());
        billing.getEvents().observe(this, e -> {
            BillingManager.PurchaseEvent event = e.consume();
            if (event != null) {
                PurchaseMessages.show(this, event);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void unlockWithCoins(PuzzlePack pack) {
        String name = getString(UiText.packName(pack.getId()));
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.pack_unlock_title)
                .setMessage(getString(R.string.pack_unlock_msg, UiText.number(pack.getCoinPrice()), name))
                .setPositiveButton(R.string.pack_unlock, (d, w) -> {
                    if (game().unlockPackWithCoins(pack)) {
                        sound(SoundManager.Sound.UNLOCK);
                        Toast.makeText(this, R.string.pack_unlocked, Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private final class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        private final List<PuzzlePack> packs;

        Adapter(List<PuzzlePack> packs) {
            this.packs = packs;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemPackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PuzzlePack pack = packs.get(position);
            ItemPackBinding b = holder.binding;
            PackManager manager = game().getPackManager();
            Inventory inventory = game().getPlayer().getInventory();
            boolean unlocked = manager.isUnlocked(pack, inventory);
            boolean completed = manager.isCompleted(pack, inventory);
            int done = inventory.getPackProgress(pack.getId());

            b.textIcon.setText(pack.getIcon());
            b.textName.setText(UiText.packName(pack.getId()));
            b.textDesc.setText(UiText.packDescription(pack.getId()));
            b.progress.setMax(pack.getLevels());
            b.progress.setProgress(Math.min(done, pack.getLevels()));
            b.textProgress.setText(completed ? getString(R.string.pack_completed)
                    : getString(R.string.pack_progress, Math.min(done + 1, pack.getLevels()), pack.getLevels()));
            b.btnSecondary.setVisibility(View.GONE);
            b.btnPrimary.setEnabled(true);

            if (unlocked) {
                int level = completed ? 0 : manager.nextLevel(pack, inventory);
                b.btnPrimary.setText(completed ? getString(R.string.pack_replay)
                        : getString(R.string.pack_play, level + 1));
                b.btnPrimary.setOnClickListener(v -> {
                    tapFeedback(v);
                    Navigator.play(PacksActivity.this, SessionConfig.pack(pack.getId(), level));
                });
            } else if (pack.isPremiumOnly()) {
                b.btnPrimary.setText(R.string.pack_premium);
                b.btnPrimary.setOnClickListener(v -> Navigator.shop(PacksActivity.this));
            } else {
                String price = pack.getProductId() != null ? game().getBilling().getFormattedPrice(pack.getProductId()) : null;
                b.btnPrimary.setText(price != null ? price : getString(R.string.shop_price_loading));
                b.btnPrimary.setEnabled(price != null);
                b.btnPrimary.setOnClickListener(v ->
                        game().getBilling().launchPurchase(PacksActivity.this, pack.getProductId()));
                if (pack.getCoinPrice() > 0) {
                    b.btnSecondary.setVisibility(View.VISIBLE);
                    b.btnSecondary.setText(getString(R.string.pack_coins, UiText.number(pack.getCoinPrice())));
                    b.btnSecondary.setOnClickListener(v -> unlockWithCoins(pack));
                }
            }
        }

        @Override
        public int getItemCount() {
            return packs.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final ItemPackBinding binding;

            Holder(ItemPackBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
