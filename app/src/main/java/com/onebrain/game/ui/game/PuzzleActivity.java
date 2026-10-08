package com.onebrain.game.ui.game;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.onebrain.game.R;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.databinding.ActivityPuzzleBinding;
import com.onebrain.game.puzzle.Hint;
import com.onebrain.game.puzzle.Memorizable;
import com.onebrain.game.puzzle.Puzzle;
import com.onebrain.game.puzzle.answer.Answer;
import com.onebrain.game.core.ChallengeLevel;
import com.onebrain.game.session.ChallengeSession;
import com.onebrain.game.session.GameSession;
import com.onebrain.game.session.SessionConfig;
import com.onebrain.game.session.SessionResult;
import com.onebrain.game.ui.base.BaseActivity;
import com.onebrain.game.ui.common.Anim;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.ui.game.render.PuzzleRenderer;
import com.onebrain.game.ui.game.render.RendererFactory;
import com.onebrain.game.ui.result.ResultActivity;
import com.onebrain.game.util.TimeFormat;

/**
 * Gameplay screen. Thin by design: state lives in {@link GameViewModel}, puzzle
 * drawing in a {@link PuzzleRenderer}. This Activity wires them together and
 * plays feedback (sound, haptics, animation).
 */
public final class PuzzleActivity extends BaseActivity implements PuzzleRenderer.Callback {

    private static final long TICK_MS = 250L;

    private ActivityPuzzleBinding binding;
    private GameViewModel viewModel;
    private PuzzleRenderer renderer;
    private Puzzle boundPuzzle;
    private boolean navigatingToResult;
    private final Handler ticker = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateTimer();
            viewModel.onTick();
            ticker.postDelayed(this, TICK_MS);
        }
    };

    public static Intent intent(Context context, SessionConfig config) {
        return new Intent(context, PuzzleActivity.class).putExtra(GameViewModel.KEY_CONFIG, config);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPuzzleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        binding.btnClose.setOnClickListener(v -> confirmQuit());
        binding.btnHint.setOnClickListener(v -> onHintClicked());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmQuit();
            }
        });

        GameSession session = viewModel.getSession();
        if (session instanceof ChallengeSession) {
            ChallengeLevel level = ((ChallengeSession) session).getLevel();
            binding.textMode.setText((level.isBoss() ? "👑 " : "")
                    + getString(R.string.path_level_title, level.getNumber()));
        } else {
            binding.textMode.setText(UiText.modeName(session.getMode()));
        }
        boolean timed = session.getTimeLimitMillis() > 0;
        binding.textTimer.setVisibility(timed || game().getSettings().isTimerVisible() ? View.VISIBLE : View.GONE);

        viewModel.getPuzzle().observe(this, this::bindPuzzle);
        viewModel.getAnswerEvents().observe(this, e -> {
            GameViewModel.AnswerEvent event = e.consume();
            if (event != null) {
                onAnswerEvent(event);
            }
        });
        viewModel.getHintEvents().observe(this, e -> {
            Hint hint = e.consume();
            if (hint != null && renderer != null) {
                renderer.applyHint(hint);
                updateHeader();
            }
        });
        viewModel.getResult().observe(this, this::openResult);
    }

    // ------------------------------------------------------------ binding

    private void bindPuzzle(Puzzle puzzle) {
        if (puzzle == null || puzzle == boundPuzzle) {
            return;
        }
        if (renderer != null) {
            renderer.release();
        }
        binding.visualContainer.removeAllViews();
        binding.inputContainer.removeAllViews();
        boundPuzzle = puzzle;

        binding.textPrompt.setText(UiText.format(this, puzzle.getPrompt()));
        binding.chipCategory.setText(UiText.categoryIcon(puzzle.getCategory()) + " "
                + getString(UiText.categoryName(puzzle.getCategory())));
        binding.chipCategory.setBackgroundTintList(ColorStateList.valueOf(
                withAlpha(ContextCompat.getColor(this, UiText.categoryColor(puzzle.getCategory())), 0x33)));
        binding.chipDifficulty.setText(UiText.difficultyName(puzzle.getDifficulty()));
        binding.chipDifficulty.setTextColor(ContextCompat.getColor(this, UiText.difficultyColor(puzzle.getDifficulty())));

        boolean resume = viewModel.isPuzzleStarted();
        renderer = RendererFactory.create(puzzle);
        renderer.bind(puzzle, binding.visualContainer, binding.inputContainer, resume, this);
        if (resume && puzzle instanceof Memorizable) {
            binding.textPrompt.setText(UiText.format(this, ((Memorizable) puzzle).getRecallPrompt()));
        }
        if (!resume) {
            Anim.enter(binding.puzzleContent);
        }
        updateHeader();
        updateTimer();
    }

    private void updateHeader() {
        GameSession session = viewModel.getSession();
        int total = session.getTotalPuzzles();
        int index = session.getProgress().getIndex();
        if (total > 1) {
            binding.textProgress.setText(getString(R.string.puzzle_progress, Math.min(index + 1, total), total));
            binding.textProgress.setVisibility(View.VISIBLE);
        } else if (total == 0) {
            binding.textProgress.setText(getString(R.string.puzzle_solved_count, session.getProgress().getSolved()));
            binding.textProgress.setVisibility(View.VISIBLE);
        } else {
            binding.textProgress.setVisibility(View.GONE);
        }

        if (session.getMaxLives() > 0) {
            StringBuilder hearts = new StringBuilder();
            for (int i = 0; i < session.getMaxLives(); i++) {
                hearts.append(i < session.getLivesLeft() ? "❤️" : "🤍");
            }
            binding.textLives.setText(hearts);
            binding.textLives.setVisibility(View.VISIBLE);
        }

        binding.textCoins.setText("🪙 " + UiText.number(game().getPlayer().getWallet().getCoins()));
        boolean hintsAllowed = session.allowsHints();
        binding.btnHint.setVisibility(hintsAllowed ? View.VISIBLE : View.GONE);
        binding.btnHint.setText("💡 " + getString(R.string.puzzle_hint, GameViewModel.HINT_COST));
        binding.btnHint.setEnabled(viewModel.canUseHint());
    }

    private void updateTimer() {
        GameSession session = viewModel.getSession();
        if (session.getTimeLimitMillis() > 0) {
            long remaining = Math.max(0L, viewModel.getTimeRemaining());
            binding.textTimer.setText("⏱ " + TimeFormat.clock(remaining + 999));
            binding.textTimer.setTextColor(ContextCompat.getColor(this,
                    remaining < 10_000L ? R.color.ob_error : R.color.ob_text));
        } else {
            binding.textTimer.setText("⏱ " + TimeFormat.clock(viewModel.getElapsed()));
        }
        binding.btnHint.setEnabled(viewModel.canUseHint());
    }

    // ------------------------------------------------------------ renderer callbacks

    @Override
    public void onAnswer(Answer answer) {
        viewModel.submit(answer);
    }

    @Override
    public void onMemorizeStarted(long durationMs) {
        viewModel.onMemorizeStarted();
        if (boundPuzzle != null) {
            binding.textPrompt.setText(UiText.format(this, boundPuzzle.getPrompt()));
        }
        updateHeader();
    }

    @Override
    public void onReady() {
        viewModel.onRendererReady();
        if (boundPuzzle instanceof Memorizable) {
            binding.textPrompt.setText(UiText.format(this, ((Memorizable) boundPuzzle).getRecallPrompt()));
            Anim.pop(binding.textPrompt);
        }
        updateHeader();
    }

    @Override
    public void onTap(View source) {
        tapFeedback(source);
    }

    // ------------------------------------------------------------ feedback

    private void onAnswerEvent(GameViewModel.AnswerEvent event) {
        if (renderer == null) {
            return;
        }
        switch (event.type) {
            case CORRECT:
                renderer.showCorrect(event.answer);
                sound(SoundManager.Sound.CORRECT);
                game().getHaptics().success(binding.getRoot());
                showFeedback(getString(R.string.feedback_correct, event.result.getScore()), R.color.ob_success);
                break;
            case RETRY:
                renderer.showWrong(event.answer, true);
                sound(SoundManager.Sound.WRONG);
                game().getHaptics().error(binding.getRoot());
                String msg = viewModel.getSession().getWrongPenaltyMillis() > 0
                        ? getString(R.string.feedback_penalty)
                        : getResources().getQuantityString(R.plurals.attempts_left, event.attemptsLeft, event.attemptsLeft);
                showFeedback(msg, R.color.ob_error);
                break;
            case FAILED:
            default:
                renderer.showWrong(event.answer, false);
                renderer.revealSolution();
                sound(SoundManager.Sound.WRONG);
                game().getHaptics().error(binding.getRoot());
                String failMsg = viewModel.getSession().getWrongPenaltyMillis() > 0
                        ? getString(R.string.feedback_penalty) : getString(R.string.feedback_failed);
                showFeedback(failMsg, R.color.ob_error);
                break;
        }
        updateHeader();
    }

    private void showFeedback(String text, int colorRes) {
        View v = binding.textFeedback;
        binding.textFeedback.setText(text);
        binding.textFeedback.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
        v.animate().cancel();
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setScaleX(0.7f);
        v.setScaleY(0.7f);
        v.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220)
                .setInterpolator(new OvershootInterpolator(2f))
                .withEndAction(() -> v.animate().alpha(0f).setStartDelay(550).setDuration(200)
                        .withEndAction(() -> v.setVisibility(View.GONE)).start())
                .start();
    }

    // ------------------------------------------------------------ hints

    private void onHintClicked() {
        GameViewModel.HintStatus status = viewModel.requestHint(false);
        if (status == GameViewModel.HintStatus.OK) {
            tapFeedback(binding.btnHint);
            updateHeader();
        } else if (status == GameViewModel.HintStatus.UNAVAILABLE) {
            Toast.makeText(this, R.string.hint_unavailable, Toast.LENGTH_SHORT).show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.hint_no_coins_title)
                    .setMessage(R.string.hint_no_coins_msg)
                    .setPositiveButton(R.string.watch_video, (d, w) -> watchAdForHint())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void watchAdForHint() {
        if (!game().getAds().isRewardedReady()) {
            Toast.makeText(this, R.string.ad_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        game().getAds().showRewarded(this, rewarded -> {
            if (rewarded) {
                viewModel.requestHint(true);
            }
            updateHeader();
        });
    }

    // ------------------------------------------------------------ navigation

    private void confirmQuit() {
        GameSession session = viewModel.getSession();
        if (session.getMode() == GameMode.DAILY) {
            showQuitDialog(R.string.quit_msg_daily, this::finish);
        } else if (viewModel.hasCompletedAnyPuzzle()) {
            showQuitDialog(R.string.quit_msg_session, viewModel::endEarly);
        } else {
            showQuitDialog(R.string.quit_msg_empty, this::finish);
        }
    }

    private void showQuitDialog(int message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.quit_title)
                .setMessage(message)
                .setPositiveButton(R.string.quit_confirm, (d, w) -> onConfirm.run())
                .setNegativeButton(R.string.keep_playing, null)
                .show();
    }

    private void openResult(SessionResult result) {
        if (result == null || navigatingToResult) {
            return;
        }
        navigatingToResult = true;
        startActivity(ResultActivity.intent(this, result, viewModel.getConfig()));
        finish();
    }

    // ------------------------------------------------------------ lifecycle

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.onHostResumed();
        ticker.removeCallbacks(tick);
        ticker.post(tick);
    }

    @Override
    protected void onPause() {
        ticker.removeCallbacks(tick);
        viewModel.onHostPaused();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ticker.removeCallbacksAndMessages(null);
        if (renderer != null) {
            renderer.release();
            renderer = null;
        }
        super.onDestroy();
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
