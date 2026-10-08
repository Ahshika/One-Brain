package com.onebrain.game.ui.game;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.onebrain.game.core.GameManager;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.player.PlayerStats;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.Hintable;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.score.ScoreResult;
import com.onebrain.game.session.DailySession;
import com.onebrain.game.session.GameSession;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.session.SessionProgress;
import com.onebrain.game.session.SessionResult;
import com.onebrain.game.util.Event;
import com.onebrain.game.util.Stopwatch;

/**
 * Holds the running session across configuration changes and drives it:
 * timing, answer checking, hints and the transition between puzzles. Session
 * config, progress and the current timer are mirrored into the
 * {@link SavedStateHandle}, so even after process death the same puzzle is
 * regenerated from its seed and the player continues where they left off.
 */
public final class GameViewModel extends ViewModel {

    public static final String KEY_CONFIG = "session_config";
    private static final String KEY_PROGRESS = "session_progress";
    private static final String KEY_ELAPSED = "puzzle_elapsed";
    private static final String KEY_MISTAKES = "puzzle_mistakes";

    public static final int HINT_COST = 25;
    private static final long DELAY_AFTER_CORRECT = 950L;
    private static final long DELAY_AFTER_FAIL = 1600L;

    public enum Phase {
        /** Puzzle bound, renderer starting (may enter MEMORIZE). */
        INTRO,
        MEMORIZE,
        PLAYING,
        FEEDBACK,
        FINISHED
    }

    public enum HintStatus {
        OK,
        UNAVAILABLE,
        NO_COINS
    }

    public static final class AnswerEvent {
        public enum Type {
            CORRECT,
            RETRY,
            FAILED
        }

        public final Type type;
        public final Answer answer;
        public final ScoreResult result;
        public final int attemptsLeft;

        AnswerEvent(Type type, Answer answer, ScoreResult result, int attemptsLeft) {
            this.type = type;
            this.answer = answer;
            this.result = result;
            this.attemptsLeft = attemptsLeft;
        }
    }

    private final SavedStateHandle state;
    private final GameManager game;
    private final SessionConfig config;
    private final GameSession session;
    private final Stopwatch stopwatch = new Stopwatch(SystemClock::elapsedRealtime);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Puzzle> puzzle = new MutableLiveData<>();
    private final MutableLiveData<Phase> phase = new MutableLiveData<>(Phase.INTRO);
    private final MutableLiveData<Event<AnswerEvent>> answerEvents = new MutableLiveData<>();
    private final MutableLiveData<Event<Hint>> hintEvents = new MutableLiveData<>();
    private final MutableLiveData<SessionResult> result = new MutableLiveData<>();

    private int mistakes;
    private boolean hostResumed;
    private boolean finished;

    public GameViewModel(SavedStateHandle state) {
        this.state = state;
        this.game = GameManager.get();
        SessionConfig cfg = state.get(KEY_CONFIG);
        if (cfg == null) {
            throw new IllegalStateException("PuzzleActivity started without a SessionConfig");
        }
        this.config = cfg;
        SessionProgress progress = state.get(KEY_PROGRESS);
        this.session = game.createSession(cfg, progress);

        Long elapsed = state.get(KEY_ELAPSED);
        Integer savedMistakes = state.get(KEY_MISTAKES);
        if (elapsed == null && session instanceof DailySession) {
            // Resume the daily puzzle timer if the player left earlier today.
            long day = ((DailySession) session).getEpochDay();
            PlayerStats stats = game.getPlayer().getStats();
            elapsed = stats.getDailyProgressElapsed(day);
            savedMistakes = stats.getDailyProgressMistakes(day);
        }
        stopwatch.reset(elapsed != null ? elapsed : 0L);
        mistakes = savedMistakes != null ? savedMistakes : 0;

        if (session.isOver()) {
            finishSession();
        } else {
            puzzle.setValue(session.getCurrentPuzzle());
        }
    }

    // ------------------------------------------------------------ observables

    public LiveData<Puzzle> getPuzzle() {
        return puzzle;
    }

    public LiveData<Phase> getPhase() {
        return phase;
    }

    public LiveData<Event<AnswerEvent>> getAnswerEvents() {
        return answerEvents;
    }

    public LiveData<Event<Hint>> getHintEvents() {
        return hintEvents;
    }

    public LiveData<SessionResult> getResult() {
        return result;
    }

    public SessionConfig getConfig() {
        return config;
    }

    public GameSession getSession() {
        return session;
    }

    public int getMistakes() {
        return mistakes;
    }

    /** True once the current puzzle accepted input (used to restore after rotation). */
    public boolean isPuzzleStarted() {
        Phase p = phase.getValue();
        return p == Phase.PLAYING || p == Phase.FEEDBACK;
    }

    public long getElapsed() {
        return stopwatch.elapsed();
    }

    public long getTimeRemaining() {
        return session.getTimeRemaining(stopwatch.elapsed());
    }

    // ------------------------------------------------------------ lifecycle

    public void onHostResumed() {
        hostResumed = true;
        if (phase.getValue() == Phase.PLAYING) {
            stopwatch.resume();
        }
    }

    public void onHostPaused() {
        hostResumed = false;
        stopwatch.pause();
        saveState();
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
        if (!finished) {
            saveState();
        }
    }

    // ------------------------------------------------------------ renderer callbacks

    public void onMemorizeStarted() {
        stopwatch.pause();
        phase.setValue(Phase.MEMORIZE);
    }

    public void onRendererReady() {
        Phase p = phase.getValue();
        if (p == Phase.INTRO || p == Phase.MEMORIZE) {
            phase.setValue(Phase.PLAYING);
            if (hostResumed) {
                stopwatch.resume();
            }
        }
    }

    // ------------------------------------------------------------ gameplay

    public void submit(Answer answer) {
        if (finished || phase.getValue() != Phase.PLAYING) {
            return;
        }
        Puzzle current = session.getCurrentPuzzle();
        if (current.check(answer)) {
            completePuzzle(current, true, answer);
            return;
        }
        mistakes++;
        session.onWrongAttempt();
        int left = session.getAttemptsPerPuzzle() - mistakes;
        if (left <= 0) {
            completePuzzle(current, false, answer);
        } else {
            answerEvents.setValue(new Event<>(new AnswerEvent(AnswerEvent.Type.RETRY, answer, null, left)));
            saveState();
        }
    }

    private void completePuzzle(Puzzle current, boolean correct, Answer answer) {
        stopwatch.pause();
        ScoreResult r = game.recordPuzzle(session, current, correct, mistakes, stopwatch.elapsed());
        session.onPuzzleCompleted(r);
        phase.setValue(Phase.FEEDBACK);
        answerEvents.setValue(new Event<>(new AnswerEvent(
                correct ? AnswerEvent.Type.CORRECT : AnswerEvent.Type.FAILED, answer, r, 0)));
        long delay = correct ? DELAY_AFTER_CORRECT : DELAY_AFTER_FAIL;
        if (session.isOver()) {
            // Commit the session right away (so nothing is lost if the app dies
            // during the feedback animation); only the navigation is delayed.
            SessionResult summary = commitSession();
            handler.postDelayed(() -> result.setValue(summary), delay);
        } else {
            handler.postDelayed(this::advance, delay);
        }
    }

    private void advance() {
        if (finished) {
            return;
        }
        mistakes = 0;
        stopwatch.reset(0L);
        phase.setValue(Phase.INTRO);
        puzzle.setValue(session.getCurrentPuzzle());
        saveState();
    }

    /** Called ~4×/s by the Activity while visible; ends Time Attack when the clock runs out. */
    public void onTick() {
        if (finished || session.getTimeLimitMillis() <= 0) {
            return;
        }
        Phase p = phase.getValue();
        if ((p == Phase.PLAYING || p == Phase.INTRO) && getTimeRemaining() <= 0) {
            stopwatch.pause();
            finishSession();
        }
    }

    public boolean canUseHint() {
        Puzzle p = puzzle.getValue();
        return session.allowsHints() && p instanceof Hintable && ((Hintable) p).canUseHint()
                && phase.getValue() == Phase.PLAYING;
    }

    /** @param free true when paid for with a rewarded ad instead of coins. */
    public HintStatus requestHint(boolean free) {
        if (!canUseHint()) {
            return HintStatus.UNAVAILABLE;
        }
        if (!free && !game.getPlayer().getWallet().spend(HINT_COST)) {
            return HintStatus.NO_COINS;
        }
        Hint hint = ((Hintable) session.getCurrentPuzzle()).useHint();
        game.persist();
        if (hint != null) {
            hintEvents.setValue(new Event<>(hint));
        }
        return HintStatus.OK;
    }

    /** Player leaves mid-session: score what was played. */
    public void endEarly() {
        if (!finished) {
            stopwatch.pause();
            finishSession();
        }
    }

    public boolean hasCompletedAnyPuzzle() {
        return session.getProgress().getPlayed() > 0;
    }

    private void finishSession() {
        if (finished) {
            return;
        }
        handler.removeCallbacksAndMessages(null);
        result.setValue(commitSession());
    }

    private SessionResult commitSession() {
        finished = true;
        if (config.getMode() == GameMode.DAILY) {
            game.getPlayer().getStats().clearDailyProgress();
        }
        phase.setValue(Phase.FINISHED);
        return game.finishSession(session);
    }

    private void saveState() {
        state.set(KEY_PROGRESS, session.getProgress());
        state.set(KEY_ELAPSED, stopwatch.elapsed());
        state.set(KEY_MISTAKES, mistakes);
        if (session instanceof DailySession && !finished) {
            game.getPlayer().getStats().saveDailyProgress(((DailySession) session).getEpochDay(),
                    stopwatch.elapsed(), mistakes);
            game.persist();
        }
    }
}
