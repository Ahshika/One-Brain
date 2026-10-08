package com.onebrain.game.ui.shop;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.onebrain.game.R;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.databinding.ActivityScrollBinding;
import com.onebrain.game.databinding.ItemShopBinding;
import com.onebrain.game.monetization.BillingManager;
import com.onebrain.game.monetization.ProductCatalog;
import com.onebrain.game.player.Inventory;
import com.onebrain.game.player.Player;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.common.Sections;
import com.onebrain.game.ui.common.UiText;

/**
 * Store front: optional rewarded coins, coin packs, permanent upgrades and
 * power-ups. Prices are always the localized Play prices.
 */
public final class ShopActivity extends BaseActivity {

    static final int FREE_COINS_REWARD = 50;
    static final int STREAK_FREEZE_PRICE = 150;

    private ActivityScrollBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScrollBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);
        binding.toolbar.setTitle(R.string.shop_title);
        binding.toolbar.getMenu().add(R.string.shop_restore).setOnMenuItemClickListener(item -> {
            game().getBilling().restorePurchases();
            return true;
        });

        BillingManager billing = game().getBilling();
        billing.connect();
        billing.getProducts().observe(this, products -> render());
        billing.getEvents().observe(this, e -> {
            BillingManager.PurchaseEvent event = e.consume();
            if (event != null) {
                PurchaseMessages.show(this, event);
                render();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        LinearLayout c = binding.content;
        c.removeAllViews();
        Player player = game().getPlayer();
        Inventory inv = player.getInventory();
        long today = game().getDaily().today();

        binding.toolbar.setSubtitle("🪙 " + UiText.number(player.getWallet().getCoins()));

        // Free coins (rewarded ad, capped per day)
        Sections.header(c, R.string.shop_free_coins);
        int left = inv.getFreeCoinAdsLeft(today);
        item(c, "🎬", getString(R.string.shop_watch_ad),
                getString(R.string.shop_watch_ad_desc, FREE_COINS_REWARD, left),
                getString(R.string.shop_watch), left > 0, v -> watchForCoins());

        // Coin packs (consumable)
        Sections.header(c, R.string.shop_coin_packs);
        productItem(c, "🪙", R.string.shop_coins_small, ProductCatalog.COINS_SMALL, false);
        productItem(c, "💰", R.string.shop_coins_medium, ProductCatalog.COINS_MEDIUM, false);
        productItem(c, "💎", R.string.shop_coins_large, ProductCatalog.COINS_LARGE, false);

        // Upgrades (non-consumable)
        Sections.header(c, R.string.shop_upgrades);
        upgradeItem(c, "🚫", R.string.shop_remove_ads, R.string.shop_remove_ads_desc,
                ProductCatalog.REMOVE_ADS, inv.isAdsRemoved());
        upgradeItem(c, "👑", R.string.shop_premium, R.string.shop_premium_desc,
                ProductCatalog.PREMIUM, inv.isPremium());
        item(c, "📦", getString(R.string.shop_special_packs), getString(R.string.shop_special_packs_desc),
                getString(R.string.shop_browse), true, v -> Navigator.packs(this));

        // Power-ups (spent with coins)
        Sections.header(c, R.string.shop_power_ups);
        item(c, "🧊", getString(R.string.shop_streak_freeze),
                getString(R.string.shop_streak_freeze_desc, inv.getStreakFreezes(), Inventory.MAX_STREAK_FREEZES),
                "🪙 " + STREAK_FREEZE_PRICE, inv.getStreakFreezes() < Inventory.MAX_STREAK_FREEZES,
                v -> buyStreakFreeze());
    }

    private void productItem(LinearLayout parent, String icon, int title, String productId, boolean owned) {
        String price = game().getBilling().getFormattedPrice(productId);
        String desc = getString(R.string.shop_coins_amount, UiText.number(ProductCatalog.coinAmount(productId)));
        item(parent, icon, getString(title), desc,
                owned ? getString(R.string.shop_owned) : price != null ? price : getString(R.string.shop_price_loading),
                !owned && price != null, v -> game().getBilling().launchPurchase(this, productId));
    }

    private void upgradeItem(LinearLayout parent, String icon, int title, int desc, String productId, boolean owned) {
        String price = game().getBilling().getFormattedPrice(productId);
        item(parent, icon, getString(title), getString(desc),
                owned ? getString(R.string.shop_owned) : price != null ? price : getString(R.string.shop_price_loading),
                !owned && price != null, v -> game().getBilling().launchPurchase(this, productId));
    }

    private void item(LinearLayout parent, String icon, String title, String desc, String action,
                      boolean enabled, View.OnClickListener listener) {
        ItemShopBinding b = ItemShopBinding.inflate(getLayoutInflater(), parent, false);
        b.textIcon.setText(icon);
        b.textTitle.setText(title);
        b.textDesc.setText(desc);
        b.btnAction.setText(action);
        b.btnAction.setEnabled(enabled);
        b.btnAction.setOnClickListener(v -> {
            tapFeedback(v);
            listener.onClick(v);
        });
        parent.addView(b.getRoot());
    }

    private void watchForCoins() {
        if (!game().getAds().isRewardedReady()) {
            Toast.makeText(this, R.string.ad_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        game().getAds().showRewarded(this, rewarded -> {
            if (!rewarded) {
                return;
            }
            Player player = game().getPlayer();
            player.getInventory().registerFreeCoinAd(game().getDaily().today());
            player.getWallet().add(FREE_COINS_REWARD);
            game().persist();
            sound(SoundManager.Sound.UNLOCK);
            Toast.makeText(this, getString(R.string.shop_free_coins_earned, FREE_COINS_REWARD), Toast.LENGTH_SHORT).show();
            render();
        });
    }

    private void buyStreakFreeze() {
        Player player = game().getPlayer();
        if (player.getInventory().getStreakFreezes() >= Inventory.MAX_STREAK_FREEZES) {
            Toast.makeText(this, R.string.shop_freeze_max, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!player.getWallet().spend(STREAK_FREEZE_PRICE)) {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show();
            return;
        }
        player.getInventory().addStreakFreeze();
        game().persist();
        sound(SoundManager.Sound.UNLOCK);
        Toast.makeText(this, R.string.shop_freeze_bought, Toast.LENGTH_SHORT).show();
        render();
    }
}
