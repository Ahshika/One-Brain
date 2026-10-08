package com.onebrain.game.ui.shop;

import android.content.Context;
import android.widget.Toast;

import com.onebrain.game.R;
import com.onebrain.game.core.GameManager;
import com.onebrain.game.audio.SoundManager;
import com.onebrain.game.monetization.BillingManager;

/** User-facing feedback for billing events. */
public final class PurchaseMessages {

    private PurchaseMessages() {
    }

    public static void show(Context context, BillingManager.PurchaseEvent event) {
        int message;
        switch (event) {
            case SUCCESS:
                message = R.string.purchase_success;
                GameManager.get().getSound().play(SoundManager.Sound.UNLOCK);
                break;
            case PENDING:
                message = R.string.purchase_pending;
                break;
            case RESTORED:
                message = R.string.purchase_restored;
                break;
            case UNAVAILABLE:
                message = R.string.billing_unavailable;
                break;
            case ERROR:
            default:
                message = R.string.purchase_error;
                break;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
