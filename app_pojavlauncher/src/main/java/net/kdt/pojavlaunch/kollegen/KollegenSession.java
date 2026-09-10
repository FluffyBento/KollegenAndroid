package net.kdt.pojavlaunch.kollegen;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;

import net.kdt.pojavlaunch.R;

import org.json.JSONObject;

public class KollegenSession {
    public static final String LOGIN_URL = KollegenApi.BASE_URL + "/api/auth/discord/login";

    private static boolean sLoggedIn;

    private KollegenSession() {}

    public static boolean isLoggedIn() {
        return sLoggedIn && cookie() != null && !cookie().isEmpty();
    }

    public static String cookie() {
        String cookie = CookieManager.getInstance().getCookie(KollegenApi.BASE_URL);
        return cookie == null ? "" : cookie;
    }

    public static void check(Activity activity, Runnable onResult) {
        KollegenApi.get("/api/profil/me", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                boolean hasUser = json instanceof org.json.JSONObject
                        && ((org.json.JSONObject) json).has("user")
                        && !((org.json.JSONObject) json).isNull("user");
                sLoggedIn = hasUser;
                onResult.run();
            }

            @Override
            public void onError(String message) {
                sLoggedIn = false;
                onResult.run();
            }
        });
    }

    public static void login(Activity activity, Runnable onSuccess) {
        WebView webView = new WebView(activity);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isLoggedIn()) {
                    onSuccess.run();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (isLoggedIn()) {
                    onSuccess.run();
                }
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.kollegen_login_title)
                .setView(webView)
                .setNegativeButton(R.string.kollegen_close, (d, w) -> {})
                .setOnDismissListener(d -> {
                    webView.destroy();
                })
                .create();
        dialog.show();
        webView.loadUrl(LOGIN_URL);
    }

    public static void logout(Activity activity, Runnable onDone) {
        KollegenApi.post("/api/auth/logout", new JSONObject(), new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                signedOut(onDone);
            }

            @Override
            public void onError(String message) {
                signedOut(onDone);
            }
        });
    }

    private static void signedOut(Runnable onDone) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
        } else {
            CookieManager.getInstance().removeAllCookie();
        }
        sLoggedIn = false;
        onDone.run();
    }
}