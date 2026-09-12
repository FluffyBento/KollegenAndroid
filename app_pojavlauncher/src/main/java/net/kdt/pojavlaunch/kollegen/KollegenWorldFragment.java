package net.kdt.pojavlaunch.kollegen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;

public class KollegenWorldFragment extends Fragment {
    public static final String TAG = "kollegen_world";

    private WebView mWebView;

    public KollegenWorldFragment() {
        super(R.layout.fragment_kollegen_world);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mWebView = view.findViewById(R.id.koll_world_web);
        view.findViewById(R.id.koll_world_close).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        setupWorld();
    }

    @SuppressWarnings("deprecation")
    private void setupWorld() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("/api/auth/discord/callback")) {
                    view.stopLoading();
                    view.loadUrl(KollegenApi.BASE_URL + "/world/");
                }
            }
        });
        mWebView.loadUrl(KollegenApi.BASE_URL + "/world/");
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) mWebView.destroy();
        super.onDestroyView();
    }
}