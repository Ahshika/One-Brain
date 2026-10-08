package com.onebrain.game.ui.common;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.ui.achievements.AchievementsActivity;
import com.onebrain.game.ui.categories.CategoriesActivity;
import com.onebrain.game.ui.game.PuzzleActivity;
import com.onebrain.game.ui.home.MainActivity;
import com.onebrain.game.ui.packs.PacksActivity;
import com.onebrain.game.ui.path.ChallengePathActivity;
import com.onebrain.game.ui.settings.PrivacyPolicyActivity;
import com.onebrain.game.ui.settings.SettingsActivity;
import com.onebrain.game.ui.shop.ShopActivity;
import com.onebrain.game.ui.stats.StatsActivity;

/**
 * Central place for screen navigation.
 *
 * <p>Flat back stack: the Home screen is always the root and at most one screen
 * sits above it. Opening a screen from anywhere but Home replaces the current
 * screen, so Back always returns straight to Home instead of walking through
 * every page visited.
 */
public final class Navigator {

    private Navigator() {
    }

    private static void open(Activity from, Intent intent) {
        from.startActivity(intent);
        if (!(from instanceof MainActivity)) {
            from.finish();
        }
    }

    public static void play(Activity from, SessionConfig config) {
        open(from, PuzzleActivity.intent(from, config));
    }

    public static void practice(Activity from) {
        open(from, CategoriesActivity.intent(from, true));
    }

    public static void categories(Activity from) {
        open(from, CategoriesActivity.intent(from, false));
    }

    public static void challengePath(Activity from) {
        open(from, new Intent(from, ChallengePathActivity.class));
    }

    public static void packs(Activity from) {
        open(from, new Intent(from, PacksActivity.class));
    }

    public static void shop(Activity from) {
        open(from, new Intent(from, ShopActivity.class));
    }

    public static void stats(Activity from) {
        open(from, new Intent(from, StatsActivity.class));
    }

    public static void achievements(Activity from) {
        open(from, new Intent(from, AchievementsActivity.class));
    }

    public static void settings(Activity from) {
        open(from, new Intent(from, SettingsActivity.class));
    }

    public static void privacyPolicy(Activity from) {
        open(from, new Intent(from, PrivacyPolicyActivity.class));
    }

    /** Opens the Play Store listing; the current screen stays (the user comes back to it). */
    public static void storeListing(Activity from) {
        String pkg = from.getPackageName();
        try {
            from.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (ActivityNotFoundException e) {
            from.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }
}
