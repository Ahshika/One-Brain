package com.onebrain.game.core;

import android.app.Application;

import com.onebrain.game.achievement.Achievement;
import com.onebrain.game.achievement.AchievementManager;
import com.onebrain.game.audio.HapticManager;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.data.SaveManager;
import com.onebrain.game.data.SettingsManager;
import com.onebrain.game.generator.PuzzleFactory;
import com.onebrain.game.monetization.AdManager;
import com.onebrain.game.monetization.BillingManager;
import com.onebrain.game.monetization.ProductCatalog;
import com.onebrain.game.player.Player;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.score.ScoreManager;
import com.onebrain.game.score.ScoreResult;
import com.onebrain.game.session.GameSession;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.session.SessionFactory;
import com.onebrain.game.session.SessionProgress;
import com.onebrain.game.session.SessionResult;

import java.util.List;

/**
 * Application-scoped facade that composes every subsystem (game logic, data,
 * persistence, audio, monetization). Activities talk to this single entry
 * point instead of wiring managers themselves. Holds only the Application
 * context, so it cannot leak an Activity.
 */
public final class GameManager {

    private static volatile GameManager instance;

    private final DateProvider dateProvider = new SystemDateProvider();
    private final SaveManager saveManager;
    private final SettingsManager settingsManager;
    private final Player player = new Player();
    private final PuzzleFactory puzzleFactory = new PuzzleFactory();
    private final ScoreManager scoreManager = new ScoreManager();
    private final DifficultyManager difficultyManager = new DifficultyManager();
    private final PackManager packManager = new PackManager();
    private final ChallengePath challengePath = new ChallengePath();
    private final AchievementManager achievementManager;
    private final DailyPuzzleManager dailyPuzzleManager;
    private final WeeklyChallengeManager weeklyChallengeManager;
    private final SessionFactory sessionFactory;
    private final SoundManager soundManager;
    private final HapticManager hapticManager;
    private final AdManager adManager;
    private final BillingManager billingManager;

    private GameManager(Application app) {
        saveManager = new SaveManager(app);
        settingsManager = new SettingsManager(app);
        saveManager.loadPlayer(player);
        achievementManager = new AchievementManager(saveManager.loadAchievements());
        dailyPuzzleManager = new DailyPuzzleManager(puzzleFactory, dateProvider);
        weeklyChallengeManager = new WeeklyChallengeManager(puzzleFactory, dateProvider);
        sessionFactory = new SessionFactory(puzzleFactory, dailyPuzzleManager, weeklyChallengeManager,
                difficultyManager, packManager, challengePath, player.getStats());
        soundManager = new SoundManager(app, settingsManager);
        hapticManager = new HapticManager(settingsManager);
        adManager = new AdManager(app, player.getInventory());
        billingManager = new BillingManager(app, this::grantProduct);
    }

    public static void init(Application app) {
        if (instance == null) {
            synchronized (GameManager.class) {
                if (instance == null) {
                    instance = new GameManager(app);
                }
            }
        }
    }

    public static GameManager get() {
        GameManager gm = instance;
        if (gm == null) {
            throw new IllegalStateException("GameManager.init() was not called");
        }
        return gm;
    }

    // ------------------------------------------------------------------ gameplay

    /** Called when the app comes to the foreground: applies streak freezes / breaks. */
    public void onAppResumed() {
        player.getStats().refreshStreak(dailyPuzzleManager.today(), player.getInventory());
        persist();
    }

    public GameSession createSession(SessionConfig config, SessionProgress restored) {
        return sessionFactory.create(config, restored);
    }

    /** Grades a finished puzzle and updates stats and coins. */
    public ScoreResult recordPuzzle(GameSession session, Puzzle puzzle, boolean correct, int mistakes, long timeMs) {
        ScoreResult result = scoreManager.evaluate(puzzle, correct, mistakes, timeMs, session.getMode());
        player.getStats().recordPuzzle(puzzle.getCategory(), puzzle.getDifficulty(), correct, mistakes,
                timeMs, result.getStars(), result.getScore());
        player.getWallet().add(result.getCoins());
        persist();
        return result;
    }

    /** Ends a session: mode bookkeeping, best scores, achievements, save. */
    public SessionResult finishSession(GameSession session) {
        SessionResult.Builder builder = session.complete(player);
        PlayerStats stats = player.getStats();
        int score = session.getProgress().getScore();
        boolean newBest = stats.submitBestScore(session.getMode(), score);
        builder.best(newBest && score > 0, stats.getBestScore(session.getMode()));

        List<Achievement> unlocked = achievementManager.evaluate(stats, player.getWallet());
        for (Achievement a : unlocked) {
            builder.addAchievement(a.getId());
        }
        saveManager.saveAchievements(achievementManager.getUnlockedIds());
        adManager.onSessionCompleted();
        persist();
        return builder.build();
    }

    public void persist() {
        saveManager.savePlayer(player);
    }

    public void resetProgress() {
        player.resetProgress();
        achievementManager.reset();
        saveManager.saveAchievements(achievementManager.getUnlockedIds());
        persist();
    }

    // ------------------------------------------------------------------ monetization

    /** Applies a Play purchase to the player's inventory/wallet. Idempotent for unlocks. */
    private void grantProduct(String productId) {
        int coins = ProductCatalog.coinAmount(productId);
        if (coins > 0) {
            player.getWallet().add(coins);
        } else if (ProductCatalog.REMOVE_ADS.equals(productId)) {
            player.getInventory().setAdsRemoved(true);
            adManager.onAdsRemoved();
        } else if (ProductCatalog.PREMIUM.equals(productId)) {
            player.getInventory().setPremium(true);
        } else {
            PuzzlePack pack = packManager.findByProduct(productId);
            if (pack != null) {
                player.getInventory().addPack(pack.getId());
            }
        }
        persist();
    }

    /** Unlocks a pack with coins. @return {@code false} if the player can't afford it. */
    public boolean unlockPackWithCoins(PuzzlePack pack) {
        if (pack.getCoinPrice() <= 0 || !player.getWallet().spend(pack.getCoinPrice())) {
            return false;
        }
        player.getInventory().addPack(pack.getId());
        persist();
        return true;
    }

    // ------------------------------------------------------------------ accessors

    public Player getPlayer() {
        return player;
    }

    public SettingsManager getSettings() {
        return settingsManager;
    }

    public PuzzleFactory getPuzzleFactory() {
        return puzzleFactory;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    public ChallengePath getChallengePath() {
        return challengePath;
    }

    public PackManager getPackManager() {
        return packManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public DailyPuzzleManager getDaily() {
        return dailyPuzzleManager;
    }

    public WeeklyChallengeManager getWeekly() {
        return weeklyChallengeManager;
    }

    public SoundManager getSound() {
        return soundManager;
    }

    public HapticManager getHaptics() {
        return hapticManager;
    }

    public AdManager getAds() {
        return adManager;
    }

    public BillingManager getBilling() {
        return billingManager;
    }
}
