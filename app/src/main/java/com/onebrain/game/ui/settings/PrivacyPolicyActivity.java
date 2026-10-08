package com.onebrain.game.ui.settings;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.onebrain.game.R;
import com.onebrain.game.databinding.ActivityScrollBinding;
import com.onebrain.game.ui.base.BaseActivity;

/**
 * Shows the bundled privacy policy offline. Google Play also requires the
 * policy at a public URL: host assets/privacy_policy.html and link it in the
 * Play Console.
 */
public final class PrivacyPolicyActivity extends BaseActivity {

    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityScrollBinding binding = ActivityScrollBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemInsets(binding.getRoot());
        setupToolbar(binding.toolbar);
        binding.toolbar.setTitle(R.string.privacy_title);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setAllowFileAccess(false);
        webView.setBackgroundColor(0);
        binding.content.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        webView.loadUrl("file:///android_asset/privacy_policy.html");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
