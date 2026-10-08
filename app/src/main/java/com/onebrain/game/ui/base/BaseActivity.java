package com.onebrain.game.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.core.GameManager;

/** Common behaviour for every screen: edge-to-edge insets, toolbar, feedback helpers. */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

    protected GameManager game() {
        return GameManager.get();
    }

    /** Pads {@code root} so content never sits under the status/navigation bars or cutouts. */
    protected void applySystemInsets(View root) {
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    protected void setupToolbar(MaterialToolbar toolbar) {
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    protected void sound(SoundManager.Sound sound) {
        game().getSound().play(sound);
    }

    protected void tapFeedback(View v) {
        game().getSound().play(SoundManager.Sound.TAP);
        game().getHaptics().tap(v);
    }
}
