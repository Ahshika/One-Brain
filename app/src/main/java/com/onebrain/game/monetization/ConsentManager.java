package com.onebrain.game.monetization;

import android.app.Activity;
import android.content.Context;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/**
 * GDPR/US-state privacy consent via Google's User Messaging Platform. Ads are
 * only requested once {@link #canRequestAds()} is true.
 */
public final class ConsentManager {

    public interface Listener {
        void onConsentGathered(boolean canRequestAds);
    }

    private final ConsentInformation consentInformation;

    public ConsentManager(Context context) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context.getApplicationContext());
    }

    public void gather(Activity activity, Listener listener) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();
        consentInformation.requestConsentInfoUpdate(activity, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                        formError -> listener.onConsentGathered(canRequestAds())),
                requestError -> listener.onConsentGathered(canRequestAds()));
    }

    public boolean canRequestAds() {
        return consentInformation.canRequestAds();
    }

    public boolean isPrivacyOptionsRequired() {
        return consentInformation.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void showPrivacyOptions(Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
        });
    }
}
