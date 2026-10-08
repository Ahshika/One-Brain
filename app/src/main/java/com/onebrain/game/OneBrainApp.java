package com.onebrain.game;

import android.app.Application;

import com.onebrain.game.core.GameManager;

public final class OneBrainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GameManager.init(this);
        GameManager.get().getSettings().applyTheme();
    }
}
