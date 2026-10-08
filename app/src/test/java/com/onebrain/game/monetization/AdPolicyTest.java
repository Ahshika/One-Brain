package com.onebrain.game.monetization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdPolicyTest {

    @Test
    public void neverInTheFirstSessions() {
        AdPolicy p = new AdPolicy();
        p.onSessionCompleted();
        p.onSessionCompleted();
        assertFalse(p.shouldShowInterstitial(10_000_000L));
    }

    @Test
    public void respectsSessionGapAndCooldown() {
        AdPolicy p = new AdPolicy();
        for (int i = 0; i < 3; i++) {
            p.onSessionCompleted();
        }
        long now = 10_000_000L;
        assertTrue(p.shouldShowInterstitial(now));
        p.onInterstitialShown(now);

        for (int i = 0; i < 3; i++) {
            p.onSessionCompleted();
        }
        assertFalse("cooldown", p.shouldShowInterstitial(now + 60_000L));
        assertTrue(p.shouldShowInterstitial(now + AdPolicy.MIN_INTERVAL_MS));

        p.onInterstitialShown(now + AdPolicy.MIN_INTERVAL_MS);
        p.onSessionCompleted();
        assertFalse("session gap", p.shouldShowInterstitial(now + 10 * AdPolicy.MIN_INTERVAL_MS));
    }
}
