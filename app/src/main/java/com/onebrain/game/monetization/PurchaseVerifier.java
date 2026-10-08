package com.onebrain.game.monetization;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/**
 * Client-side signature check of Play purchases using the app's licensing key
 * (Play Console → Monetization setup). This deters casual tampering; for real
 * protection also verify tokens on a backend with the Play Developer API.
 * When no key is configured, verification is skipped.
 */
public final class PurchaseVerifier {

    private static final String TAG = "PurchaseVerifier";

    private final String base64PublicKey;

    public PurchaseVerifier(String base64PublicKey) {
        this.base64PublicKey = base64PublicKey;
    }

    public boolean isValid(String signedData, String signature) {
        if (TextUtils.isEmpty(base64PublicKey)) {
            return true;
        }
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(signature)) {
            return false;
        }
        try {
            byte[] keyBytes = Base64.decode(base64PublicKey, Base64.DEFAULT);
            PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(key);
            verifier.update(signedData.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.decode(signature, Base64.DEFAULT));
        } catch (Exception e) {
            Log.e(TAG, "Signature verification failed", e);
            return false;
        }
    }
}
