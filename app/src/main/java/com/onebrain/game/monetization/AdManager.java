package com.onebrain.game.monetization;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.onebrain.game.BuildConfig;
import com.onebrain.game.player.Inventory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AdMob facade: consent, SDK initialization, preloading and polite display of
 * interstitial and rewarded ads. It never keeps an Activity reference beyond a
 * single show() call, so it cannot leak screens.
 */
public final class AdManager {

    public interface RewardListener {
        /** Called on the main thread after the ad closes. */
        void onRewardResult(boolean rewarded);
    }

    private static final String TAG = "AdManager";
    private static final long RETRY_DELAY_MS = 30_000L;

    private final Context appContext;
    private final Inventory inventory;
    private final ConsentManager consent;
    private final AdPolicy policy = new AdPolicy();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean initStarted = new AtomicBoolean(false);
    private final MutableLiveData<Boolean> ready = new MutableLiveData<>(false);

    private InterstitialAd interstitial;
    private RewardedAd rewarded;
    private boolean loadingInterstitial;
    private boolean loadingRewarded;
    private boolean showingFullScreen;

    public AdManager(Context context, Inventory inventory) {
        this.appContext = context.getApplicationContext();
        this.inventory = inventory;
        this.consent = new ConsentManager(appContext);
    }

    /** Call from the first Activity. Shows the consent form if required, then initializes ads. */
    public void gatherConsentAndInitialize(Activity activity) {
        consent.gather(activity, canRequestAds -> {
            if (canRequestAds) {
                initialize();
            }
        });
        // Consent from a previous session lets us start immediately.
        if (consent.canRequestAds()) {
            initialize();
        }
    }

    private void initialize() {
        if (!initStarted.compareAndSet(false, true)) {
            return;
        }
        // MobileAds.initialize does disk I/O; keep it off the main thread.
        new Thread(() -> MobileAds.initialize(appContext, status -> main.post(() -> {
            ready.setValue(true);
            preloadInterstitial();
            preloadRewarded();
        })), "ads-init").start();
    }

    /** Emits {@code true} once ads can be requested. */
    public LiveData<Boolean> isReady() {
        return ready;
    }

    public boolean shouldShowBanners() {
        return !inventory.isAdsRemoved() && Boolean.TRUE.equals(ready.getValue());
    }

    public ConsentManager getConsent() {
        return consent;
    }

    // ------------------------------------------------------------ interstitial

    public void onSessionCompleted() {
        policy.onSessionCompleted();
    }

    /**
     * Shows an interstitial if the policy allows; {@code then} always runs exactly
     * once (immediately when no ad is shown).
     */
    public void maybeShowInterstitial(Activity activity, Runnable then) {
        long now = SystemClock.elapsedRealtime();
        InterstitialAd ad = interstitial;
        if (inventory.isAdsRemoved() || ad == null || showingFullScreen || !policy.shouldShowInterstitial(now)) {
            then.run();
            return;
        }
        interstitial = null;
        showingFullScreen = true;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                showingFullScreen = false;
                preloadInterstitial();
                then.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                showingFullScreen = false;
                preloadInterstitial();
                then.run();
            }
        });
        policy.onInterstitialShown(now);
        ad.show(activity);
    }

    private void preloadInterstitial() {
        if (inventory.isAdsRemoved() || interstitial != null || loadingInterstitial || !Boolean.TRUE.equals(ready.getValue())) {
            return;
        }
        loadingInterstitial = true;
        InterstitialAd.load(appContext, BuildConfig.AD_INTERSTITIAL_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        loadingInterstitial = false;
                        interstitial = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        loadingInterstitial = false;
                        Log.d(TAG, "Interstitial failed: " + error.getMessage());
                        main.postDelayed(AdManager.this::preloadInterstitial, RETRY_DELAY_MS);
                    }
                });
    }

    // ------------------------------------------------------------ rewarded

    public boolean isRewardedReady() {
        return rewarded != null && !showingFullScreen;
    }

    /** Rewarded ads are always optional and remain available even with Remove Ads. */
    public void showRewarded(Activity activity, RewardListener listener) {
        RewardedAd ad = rewarded;
        if (ad == null || showingFullScreen) {
            preloadRewarded();
            listener.onRewardResult(false);
            return;
        }
        rewarded = null;
        showingFullScreen = true;
        final boolean[] earned = {false};
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                showingFullScreen = false;
                preloadRewarded();
                listener.onRewardResult(earned[0]);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                showingFullScreen = false;
                preloadRewarded();
                listener.onRewardResult(false);
            }
        });
        ad.show(activity, rewardItem -> earned[0] = true);
    }

    private void preloadRewarded() {
        if (rewarded != null || loadingRewarded || !Boolean.TRUE.equals(ready.getValue())) {
            return;
        }
        loadingRewarded = true;
        RewardedAd.load(appContext, BuildConfig.AD_REWARDED_ID, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        loadingRewarded = false;
                        rewarded = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        loadingRewarded = false;
                        Log.d(TAG, "Rewarded failed: " + error.getMessage());
                        main.postDelayed(AdManager.this::preloadRewarded, RETRY_DELAY_MS);
                    }
                });
    }

    /** Called after "Remove Ads" is purchased. */
    public void onAdsRemoved() {
        interstitial = null;
    }
}
