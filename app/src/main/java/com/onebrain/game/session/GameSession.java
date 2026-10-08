package com.onebrain.game.session;

import com.onebrain.game.core.GameMode;
import com.onebrain.game.player.Player;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.score.ScoreResult;
import com.onebrain.game.util.SeedUtil;

/**
 * Abstract game mode. Subclasses define which puzzles are served and the rules
 * (attempts, lives, time limit, completion rewards) — classic polymorphism: the
 * ViewModel drives every mode through this one API.
 */
public abstract class GameSession {

    private final GameMode mode;
    private final long baseSeed;
    private final SessionProgress progress;
    private Puzzle current;

    protected GameSession(GameMode mode, long baseSeed, SessionProgress restored) {
        this.mode = mode;
        this.baseSeed = baseSeed;
        this.progress = restored != null ? restored : new SessionProgress();
    }

    /** Builds the puzzle at {@code index}; must be deterministic for (index, seed). */
    protected abstract Puzzle createPuzzle(int index, long seed);

    /** Wrong answers allowed per puzzle before it counts as failed. */
    public abstract int getAttemptsPerPuzzle();

    /** Mode-specific bookkeeping when the session ends (streaks, packs, runs…). */
    protected abstract void onCompleted(Player player, SessionResult.Builder result);

    /** Fixed number of puzzles, or 0 if open-ended. */
    public int getTotalPuzzles() {
        return 0;
    }

    /** Session time limit, or 0 for none. */
    public long getTimeLimitMillis() {
        return 0L;
    }

    /** Lives, or 0 if the mode has no lives. */
    public int getMaxLives() {
        return 0;
    }

    public long getWrongPenaltyMillis() {
        return 0L;
    }

    public boolean allowsHints() {
        return true;
    }

    public final Puzzle getCurrentPuzzle() {
        if (current == null) {
            current = createPuzzle(progress.getIndex(), SeedUtil.mix(baseSeed, progress.getIndex()));
        }
        return current;
    }

    public final void onWrongAttempt() {
        progress.addMistake(getWrongPenaltyMillis());
    }

    public final void onPuzzleCompleted(ScoreResult result) {
        progress.addPuzzle(result);
        current = null;
    }

    public final int getLivesLeft() {
        return getMaxLives() == 0 ? 0 : Math.max(0, getMaxLives() - progress.getFailed());
    }

    /** Remaining time given the elapsed time of the puzzle in progress. */
    public final long getTimeRemaining(long currentPuzzleElapsed) {
        return getTimeLimitMillis() - progress.getTimeMs() - progress.getPenaltyMs() - currentPuzzleElapsed;
    }

    public boolean isOver() {
        if (getTotalPuzzles() > 0 && progress.getIndex() >= getTotalPuzzles()) {
            return true;
        }
        if (getMaxLives() > 0 && getLivesLeft() == 0) {
            return true;
        }
        return getTimeLimitMillis() > 0 && getTimeRemaining(0) <= 0;
    }

    /** Builds the result common to all modes, then lets the subclass add its part. */
    public final SessionResult.Builder complete(Player player) {
        int played = progress.getPlayed();
        int stars = played == 0 ? 0 : Math.round(progress.getStarsSum() / (float) played);
        SessionResult.Builder b = new SessionResult.Builder(mode)
                .counts(played, progress.getSolved())
                .success(progress.getSolved() > 0)
                .score(progress.getScore())
                .time(progress.getTimeMs())
                .stars(stars)
                .coins(progress.getCoins());
        onCompleted(player, b);
        return b;
    }

    public final GameMode getMode() {
        return mode;
    }

    public final long getBaseSeed() {
        return baseSeed;
    }

    public final SessionProgress getProgress() {
        return progress;
    }
}
