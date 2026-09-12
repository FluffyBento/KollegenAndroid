package net.kdt.pojavlaunch.kollegen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;

public class KollegenWebFragment extends Fragment {
    public static final String TAG = "kollegen_web";

    private static final String ARG_PATH = "path";
    private static final String ARG_TITLE = "title";

    private WebView mWebView;
    private String mPath;

    public KollegenWebFragment() {
        super(R.layout.fragment_kollegen_web);
    }

    public static Bundle args(String path, String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PATH, path);
        bundle.putString(ARG_TITLE, title);
        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        mPath = args == null ? "/" : args.getString(ARG_PATH, "/");
        String title = args == null ? "" : args.getString(ARG_TITLE, "");
        TextView titleView = view.findViewById(R.id.koll_web_title);
        if (titleView != null) titleView.setText(title);
        mWebView = view.findViewById(R.id.koll_web_dom);
        view.findViewById(R.id.koll_web_close).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        setupWeb();
    }

    @SuppressWarnings("deprecation")
    private void setupWeb() {
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
                    view.loadUrl(KollegenApi.BASE_URL + mPath);
                }
            }
        });
        mWebView.loadUrl(KollegenApi.BASE_URL + mPath);
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) mWebView.destroy();
        super.onDestroyView();
    }
}