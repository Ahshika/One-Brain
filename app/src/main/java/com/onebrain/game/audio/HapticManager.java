package com.onebrain.game.audio;

import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;

import com.onebrain.game.data.SettingsManager;

/**
 * Haptics through {@link View#performHapticFeedback}: no VIBRATE permission
 * needed and it respects the system's touch-feedback setting.
 */
public final class HapticManager {

    private final SettingsManager settings;

    public HapticManager(SettingsManager settings) {
        this.settings = settings;
    }

    public void tap(View view) {
        perform(view, HapticFeedbackConstants.KEYBOARD_TAP);
    }

    public void success(View view) {
        perform(view, Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.CONTEXT_CLICK);
    }

    public void error(View view) {
        perform(view, Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.LONG_PRESS);
    }

    private void perform(View view, int constant) {
        if (view != null && settings.isHapticsEnabled()) {
            view.performHapticFeedback(constant);
        }
    }
}
