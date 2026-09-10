package net.kdt.pojavlaunch.kollegen;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
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
        if (activity == null) return;
        final boolean[] done = {false};

        final WebView webView = new WebView(activity);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportMultipleWindows(true);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.setBackgroundColor(android.graphics.Color.WHITE);
        webView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, 560,
                        activity.getResources().getDisplayMetrics())));

        final Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] running = {false};
        final boolean[] polling = {true};
        final AlertDialog[] dialogRef = {null};

        final Runnable[] poll = new Runnable[1];
        poll[0] = () -> {
            if (done[0] || !polling[0]) return;
            if (running[0]) return;
            running[0] = true;
            check(activity, () -> {
                running[0] = false;
                if (done[0]) return;
                if (isLoggedIn()) {
                    done[0] = true;
                    polling[0] = false;
                    AlertDialog dialog = dialogRef[0];
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    onSuccess.run();
                } else if (polling[0]) {
                    handler.postDelayed(poll[0], 900);
                }
            });
        };

        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return done[0];
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                handler.post(poll[0]);
            }
        };
        webView.setWebViewClient(client);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
                resultMsg.sendToTarget();
                return true;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.kollegen_login_title)
                .setView(webView)
                .setNegativeButton(R.string.kollegen_close, (d, w) -> {})
                .setOnDismissListener(d -> {
                    polling[0] = false;
                    webView.destroy();
                })
                .create();
        dialogRef[0] = dialog;
        dialog.show();
        webView.loadUrl(LOGIN_URL);
        handler.postDelayed(poll[0], 600);
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