package com.onebrain.game.session;

import com.onebrain.game.core.ChallengePath;
import com.onebrain.game.core.DailyPuzzleManager;
import com.onebrain.game.core.DifficultyManager;
import com.onebrain.game.core.PackManager;
import com.onebrain.game.core.PuzzlePack;
import com.onebrain.game.core.WeeklyChallengeManager;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.player.PlayerStats;

/** Creates the right {@link GameSession} subclass for a {@link SessionConfig}. */
public final class SessionFactory {

    private final PuzzleFactory puzzles;
    private final DailyPuzzleManager daily;
    private final WeeklyChallengeManager weekly;
    private final DifficultyManager difficulty;
    private final PackManager packs;
    private final PlayerStats stats;
    private final ChallengePath path;

    public SessionFactory(PuzzleFactory puzzles, DailyPuzzleManager daily, WeeklyChallengeManager weekly,
                          DifficultyManager difficulty, PackManager packs, ChallengePath path, PlayerStats stats) {
        this.path = path;
        this.puzzles = puzzles;
        this.daily = daily;
        this.weekly = weekly;
        this.difficulty = difficulty;
        this.packs = packs;
        this.stats = stats;
    }

    public GameSession create(SessionConfig config, SessionProgress restored) {
        switch (config.getMode()) {
            case DAILY:
                return new DailySession(daily, config.getSeed(), restored);
            case PRACTICE:
                return new PracticeSession(puzzles, difficulty, stats, config.getCategory(),
                        config.getDifficulty(), config.getSeed(), restored);
            case ENDLESS:
                return new EndlessSession(puzzles, config.getSeed(), restored);
            case TIME_ATTACK:
                return new TimeAttackSession(puzzles, config.getSeed(), restored);
            case WEEKLY:
                return new WeeklySession(weekly, (int) config.getSeed(), restored);
            case PACK: {
                PuzzlePack pack = packs.find(config.getPackId());
                if (pack == null) {
                    throw new IllegalArgumentException("Unknown pack " + config.getPackId());
                }
                return new PackSession(puzzles, pack, config.getPackLevel(), restored);
            }
            case CHALLENGE:
                return new ChallengeSession(puzzles, path, config.getPackLevel(), restored);
            default:
                throw new IllegalArgumentException("Unsupported mode " + config.getMode());
        }
    }
}
