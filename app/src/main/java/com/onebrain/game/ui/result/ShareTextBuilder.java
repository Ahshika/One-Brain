package com.onebrain.game.ui.result;

import android.content.Context;

import com.onebrain.game.R;
import com.onebrain.game.core.GameMode;
import com.onebrain.game.session.SessionResult;
import com.onebrain.game.ui.common.UiText;
import com.onebrain.game.util.TimeFormat;

/**
 * Builds a spoiler-free share text: puzzle number, category, stars, time and
 * score. The answer itself is never included.
 */
public final class ShareTextBuilder {

    private ShareTextBuilder() {
    }

    public static String build(Context context, SessionResult r) {
        StringBuilder sb = new StringBuilder();
        if (r.getMode() == GameMode.DAILY) {
            sb.append("🧠 ").append(context.getString(R.string.share_daily_header, r.getDayNumber())).append('\n');
            if (r.getCategory() != null && r.getDifficulty() != null) {
                sb.append(UiText.categoryIcon(r.getCategory())).append(' ')
                        .append(context.getString(R.string.result_subtitle,
                                context.getString(UiText.categoryName(r.getCategory())),
                                context.getString(UiText.difficultyName(r.getDifficulty()))))
                        .append('\n');
            }
            if (r.isSuccess()) {
                sb.append(context.getString(R.string.share_line_result, UiText.stars(r.getStars()),
                        TimeFormat.clock(r.getTimeMs()), UiText.number(r.getScore()))).append('\n');
            } else {
                sb.append(context.getString(R.string.share_line_failed)).append('\n');
            }
            if (r.getStreak() > 0) {
                sb.append(context.getString(R.string.share_line_streak, r.getStreak())).append('\n');
            }
        } else if (r.getMode() == GameMode.CHALLENGE) {
            sb.append("🧠 ").append(context.getString(R.string.share_session_header,
                    context.getString(R.string.mode_challenge))).append('\n');
            int number = r.getChallengeLevel() + 1;
            sb.append(r.isSuccess()
                    ? context.getString(R.string.share_line_challenge, number, UiText.stars(r.getStars()))
                    : context.getString(R.string.share_line_challenge_failed, number)).append('\n');
        } else {
            sb.append("🧠 ").append(context.getString(R.string.share_session_header,
                    context.getString(UiText.modeName(r.getMode())))).append('\n');
            sb.append(context.getString(R.string.share_line_session, r.getSolved(), UiText.number(r.getScore())))
                    .append('\n');
        }
        sb.append(context.getString(R.string.share_footer, context.getPackageName()));
        return sb.toString();
    }
}
