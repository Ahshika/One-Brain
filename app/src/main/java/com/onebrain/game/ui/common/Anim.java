package com.onebrain.game.ui.common;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.onebrain.game.ui.common.UiText;

/** Small, reusable, hardware-accelerated animations. */
public final class Anim {

    private Anim() {
    }

    public static void shake(View view) {
        float d = view.getResources().getDisplayMetrics().density * 8f;
        ObjectAnimator a = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, -d, d, -d * 0.6f, d * 0.6f, -d * 0.3f, 0);
        a.setDuration(360);
        a.start();
    }

    public static void pop(View view) {
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(220)
                .setInterpolator(new OvershootInterpolator(2.5f)).start();
    }

    public static void enter(View view) {
        view.setAlpha(0f);
        view.setTranslationY(view.getResources().getDisplayMetrics().density * 16f);
        view.animate().alpha(1f).translationY(0f).setDuration(280)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    public static void starIn(View view, long delay) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setRotation(-90f);
        view.animate().scaleX(1f).scaleY(1f).rotation(0f).setStartDelay(delay).setDuration(420)
                .setInterpolator(new OvershootInterpolator(3f)).start();
    }

    /** Counts a number up from 0, e.g. the score on the results screen. */
    public static ValueAnimator countUp(TextView view, int target, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> view.setText(UiText.number((Integer) a.getAnimatedValue())));
        animator.start();
        return animator;
    }
}
