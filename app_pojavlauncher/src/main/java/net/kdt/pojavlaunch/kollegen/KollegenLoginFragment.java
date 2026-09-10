package net.kdt.pojavlaunch.kollegen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class KollegenLoginFragment extends Fragment {
    public static final String TAG = "kollegen_login";

    private WebView mWebView;
    private boolean mFinished = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Runnable mPoll = new Runnable() {
        @Override
        public void run() {
            if (mFinished || !isAdded()) return;
            KollegenSession.check(requireActivity(), () -> {
                if (mFinished || !isAdded()) return;
                if (KollegenSession.isLoggedIn()) {
                    finishLogin();
                } else {
                    mHandler.postDelayed(mPoll, 900);
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mWebView = (WebView) inflater.inflate(R.layout.fragment_kollegen_login, container, false);
        setup();
        return mWebView;
    }

    @SuppressWarnings("deprecation")
    private void setup() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        mWebView.setBackgroundColor(android.graphics.Color.WHITE);
        mWebView.setWebViewClient(new TrackClient());
        mWebView.loadUrl(KollegenSession.LOGIN_URL);
        mHandler.postDelayed(mPoll, 600);
    }

    private void finishLogin() {
        if (mFinished) return;
        mFinished = true;
        Runnable onSuccess = KollegenSession.takePendingSuccess();
        if (isAdded()) Tools.backToMainMenu(requireActivity());
        if (onSuccess != null) onSuccess.run();
    }

    @Override
    public void onDestroyView() {
        mFinished = true;
        mHandler.removeCallbacks(mPoll);
        super.onDestroyView();
    }

    private void triggerCheck() {
        if (mFinished || !isAdded()) return;
        mHandler.removeCallbacks(mPoll);
        mHandler.post(mPoll);
        mHandler.postDelayed(mPoll, 900);
    }

    private class TrackClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("/api/auth/discord/callback")) triggerCheck();
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.contains("/api/auth/discord/callback")) triggerCheck();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            triggerCheck();
        }
    }
}