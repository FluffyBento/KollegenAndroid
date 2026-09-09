package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;

public class KollegenSocialFragment extends Fragment {
    public static final String TAG = "kollegen_social";
    private static final String KOLLEGEN_URL = "https://kollegen.me/freunde";
    private WebView mWebview;

    public KollegenSocialFragment() {
        super(R.layout.fragment_kollegen_social);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mWebview = view.findViewById(R.id.kollegen_social_webview);
        WebSettings settings = mWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieManager.getInstance().setAcceptCookie(true);
        mWebview.setWebViewClient(new WebViewClient());
        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                WebView child = new WebView(view.getContext());
                child.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView inner, String url) {
                        mWebview.loadUrl(url);
                        return true;
                    }
                });
                ((WebView.WebViewTransport) resultMsg.obj).setWebView(child);
                resultMsg.sendToTarget();
                return true;
            }
        });
        if (savedInstanceState != null && mWebview.restoreState(savedInstanceState) != null) {
            return;
        }
        mWebview.loadUrl(KOLLEGEN_URL);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWebview != null) mWebview.saveState(outState);
    }

    public boolean canGoBack() {
        return mWebview != null && mWebview.canGoBack();
    }

    public void goBack() {
        if (mWebview != null) mWebview.goBack();
    }

    @Override
    public void onDestroy() {
        if (mWebview != null) {
            mWebview.destroy();
            mWebview = null;
        }
        super.onDestroy();
    }
}