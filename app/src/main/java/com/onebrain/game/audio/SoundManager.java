package com.onebrain.game.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.onebrain.game.R;
import com.onebrain.game.data.SettingsManager;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Low-latency sound effects through a single small {@link SoundPool}. Clips are
 * tiny OGG files decoded once; an idle SoundPool costs no CPU or battery.
 */
public final class SoundManager {

    public enum Sound {
        TAP(R.raw.sfx_tap, 0.45f),
        CORRECT(R.raw.sfx_correct, 0.8f),
        WRONG(R.raw.sfx_wrong, 0.7f),
        WIN(R.raw.sfx_win, 0.85f),
        UNLOCK(R.raw.sfx_unlock, 0.85f),
        STAR(R.raw.sfx_star, 0.6f);

        final int resId;
        final float volume;

        Sound(int resId, float volume) {
            this.resId = resId;
            this.volume = volume;
        }
    }

    private final SettingsManager settings;
    private final SoundPool pool;
    private final Map<Sound, Integer> soundIds = new EnumMap<>(Sound.class);
    private final Set<Integer> loaded = Collections.synchronizedSet(new HashSet<>());

    public SoundManager(Context context, SettingsManager settings) {
        this.settings = settings;
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attributes).build();
        pool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                loaded.add(sampleId);
            }
        });
        Context app = context.getApplicationContext();
        for (Sound s : Sound.values()) {
            soundIds.put(s, pool.load(app, s.resId, 1));
        }
    }

    public void play(Sound sound) {
        if (!settings.isSoundEnabled()) {
            return;
        }
        Integer id = soundIds.get(sound);
        if (id != null && loaded.contains(id)) {
            pool.play(id, sound.volume, sound.volume, 1, 0, 1f);
        }
    }

    /** Pitch-shifted play, used for ascending star chimes. */
    public void play(Sound sound, float rate) {
        if (!settings.isSoundEnabled()) {
            return;
        }
        Integer id = soundIds.get(sound);
        if (id != null && loaded.contains(id)) {
            pool.play(id, sound.volume, sound.volume, 1, 0, Math.max(0.5f, Math.min(2f, rate)));
        }
    }

    public void release() {
        pool.release();
    }
}
