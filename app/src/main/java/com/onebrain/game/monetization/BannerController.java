package com.onebrain.game.monetization;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.onebrain.game.BuildConfig;

/**
 * Lifecycle-aware adaptive banner. Pauses with the screen, is destroyed with
 * it (no leaks), and disappears as soon as "Remove Ads" is owned.
 */
public final class BannerController implements DefaultLifecycleObserver {

    private final AppCompatActivity activity;
    private final FrameLayout container;
    private final AdManager adManager;
    private AdView adView;

    private BannerController(AppCompatActivity activity, FrameLayout container, AdManager adManager) {
        this.activity = activity;
        this.container = container;
        this.adManager = adManager;
    }

    /** Attaches a banner to {@code container} once ads are ready. */
    public static void attach(AppCompatActivity activity, FrameLayout container, AdManager adManager) {
        BannerController controller = new BannerController(activity, container, adManager);
        activity.getLifecycle().addObserver(controller);
        adManager.isReady().observe(activity, ready -> {
            if (Boolean.TRUE.equals(ready)) {
                controller.load();
            }
        });
    }

    private void load() {
        if (adView != null || !adManager.shouldShowBanners()) {
            return;
        }
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        adView = new AdView(activity);
        adView.setAdUnitId(BuildConfig.AD_BANNER_ID);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp));
        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(View.VISIBLE);
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        if (!adManager.shouldShowBanners()) {
            destroyBanner();
            container.setVisibility(View.GONE);
        } else if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        destroyBanner();
        owner.getLifecycle().removeObserver(this);
    }

    private void destroyBanner() {
        if (adView != null) {
            container.removeView(adView);
            adView.destroy();
            adView = null;
        }
    }
}
