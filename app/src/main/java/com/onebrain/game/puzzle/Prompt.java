package com.onebrain.game.puzzle;

/** A localizable message: a {@link PromptKey} plus format arguments. */
public final class Prompt {

    private final PromptKey key;
    private final Object[] args;

    public Prompt(PromptKey key, Object... args) {
        this.key = key;
        this.args = args == null ? new Object[0] : args.clone();
    }

    public PromptKey getKey() {
        return key;
    }

    public Object[] getArgs() {
        return args.clone();
    }
}
