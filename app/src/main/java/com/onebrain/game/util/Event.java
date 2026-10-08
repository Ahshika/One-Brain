package com.onebrain.game.util;

/**
 * Wrapper for one-shot LiveData events (navigation, sounds, toasts) so they are
 * not replayed after a configuration change.
 */
public final class Event<T> {

    private final T content;
    private boolean handled;

    public Event(T content) {
        this.content = content;
    }

    /** Returns the content once; subsequent calls return {@code null}. */
    public T consume() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }

    public T peek() {
        return content;
    }
}
