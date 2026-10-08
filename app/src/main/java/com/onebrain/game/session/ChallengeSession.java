package com.onebrain.game.session;

import com.onebrain.game.core.ChallengeLevel;
import com.onebrain.game.core.ChallengePath;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.ChallengeProgress;
import com.onebrain.game.player.Player;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.util.SeedUtil;

/**
 * One Challenge Path level. Every puzzle must be solved within its attempts
 * (and the whole level within the clock, if any); one failed puzzle fails the
 * level. Clearing it unlocks the next level and pays a coin reward.
 */
public final class ChallengeSession extends GameSession {

    private final PuzzleFactory factory;
    private final ChallengePath path;
    private final ChallengeLevel level;

    public ChallengeSession(PuzzleFactory factory, ChallengePath path, int levelIndex, SessionProgress restored) {
        this(factory, path, path.level(levelIndex), restored);
    }

    private ChallengeSession(PuzzleFactory factory, ChallengePath path, ChallengeLevel level, SessionProgress restored) {
        super(GameMode.CHALLENGE, level.getSeed(), restored);
        this.factory = factory;
        this.path = path;
        this.level = level;
    }

    @Override
    protected Puzzle createPuzzle(int index, long seed) {
        return factory.create(level.getCategory(index), level.getDifficulty(index),
                SeedUtil.mix(level.getSeed(), index));
    }

    @Override
    public int getAttemptsPerPuzzle() {
        return level.getAttemptsPerPuzzle();
    }

    @Override
    public int getTotalPuzzles() {
        return level.getPuzzleCount();
    }

    @Override
    public long getTimeLimitMillis() {
        return level.getTimeLimitMillis();
    }

    @Override
    public boolean allowsHints() {
        return level.isHintsAllowed();
    }

    @Override
    public boolean isOver() {
        return getProgress().getFailed() > 0 || super.isOver();
    }

    public ChallengeLevel getLevel() {
        return level;
    }

    public boolean isPassed() {
        return getProgress().getFailed() == 0 && getProgress().getSolved() >= level.getPuzzleCount();
    }

    @Override
    protected void onCompleted(Player player, SessionResult.Builder result) {
        boolean passed = isPassed();
        int bonus = 0;
        boolean stageCleared = false;
        if (passed) {
            ChallengeProgress progress = player.getStats().getChallenge();
            int stars = Math.round(getProgress().getStarsSum() / (float) level.getPuzzleCount());
            boolean first = progress.recordClear(level.getIndex(), stars);
            if (first) {
                bonus = path.clearReward(level);
                player.getWallet().add(bonus);
                stageCleared = level.isBoss();
            }
        }
        result.success(passed)
                .difficulty(level.getPeakDifficulty())
                .coins(getProgress().getCoins() + bonus)
                .challenge(level.getIndex(), level.getStage(), passed, stageCleared, bonus);
    }
}
