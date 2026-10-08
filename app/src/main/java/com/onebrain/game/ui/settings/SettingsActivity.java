package com.onebrain.game.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.onebrain.game.BuildConfig;
import com.onebrain.game.R;
import com.onebrain.game.data.SettingsManager;
import com.onebrain.game.databinding.ActivitySettingsBinding;
import com.onebrain.game.monetization.BillingManager;
import com.onebrain.game.monetization.ConsentManager;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Navigator;
import com.onebrain.game.ui.shop.PurchaseMessages;

public final class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);

        SettingsManager settings = game().getSettings();
        binding.switchSound.setChecked(settings.isSoundEnabled());
        binding.switchHaptics.setChecked(settings.isHapticsEnabled());
        binding.switchTimer.setChecked(settings.isTimerVisible());
        binding.switchSound.setOnCheckedChangeListener((b, checked) -> settings.setSoundEnabled(checked));
        binding.switchHaptics.setOnCheckedChangeListener((b, checked) -> {
            settings.setHapticsEnabled(checked);
            game().getHaptics().tap(b);
        });
        binding.switchTimer.setOnCheckedChangeListener((b, checked) -> settings.setTimerVisible(checked));

        switch (settings.getThemeMode()) {
            case LIGHT:
                binding.toggleTheme.check(R.id.btn_theme_light);
                break;
            case DARK:
                binding.toggleTheme.check(R.id.btn_theme_dark);
                break;
            default:
                binding.toggleTheme.check(R.id.btn_theme_system);
                break;
        }
        binding.toggleTheme.addOnButtonCheckedListener((group, id, checked) -> {
            if (!checked) {
                return;
            }
            SettingsManager.ThemeMode mode = id == R.id.btn_theme_light ? SettingsManager.ThemeMode.LIGHT
                    : id == R.id.btn_theme_dark ? SettingsManager.ThemeMode.DARK : SettingsManager.ThemeMode.SYSTEM;
            if (mode != settings.getThemeMode()) {
                settings.setThemeMode(mode); // recreates activities with the new theme
            }
        });

        ConsentManager consent = game().getAds().getConsent();
        binding.rowPrivacyOptions.setVisibility(consent.isPrivacyOptionsRequired() ? View.VISIBLE : View.GONE);
        binding.rowPrivacyOptions.setOnClickListener(v -> consent.showPrivacyOptions(this));
        binding.rowPrivacyPolicy.setOnClickListener(v -> Navigator.privacyPolicy(this));
        binding.rowRestore.setOnClickListener(v -> game().getBilling().restorePurchases());
        binding.rowRate.setOnClickListener(v -> Navigator.storeListing(this));
        binding.rowReset.setOnClickListener(v -> confirmReset());
        binding.textVersion.setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME));

        game().getBilling().getEvents().observe(this, e -> {
            BillingManager.PurchaseEvent event = e.consume();
            if (event != null) {
                PurchaseMessages.show(this, event);
            }
        });
    }

    private void confirmReset() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_reset_title)
                .setMessage(R.string.settings_reset_msg)
                .setPositiveButton(R.string.settings_reset_confirm, (d, w) -> {
                    game().resetProgress();
                    Toast.makeText(this, R.string.settings_reset_done, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
