package net.kdt.pojavlaunch.kollegen;

import android.app.Activity;
import android.webkit.CookieManager;

import androidx.fragment.app.FragmentActivity;

import net.kdt.pojavlaunch.Tools;

import org.json.JSONObject;

public class KollegenSession {
    public static final String LOGIN_URL = KollegenApi.BASE_URL + "/api/auth/discord/login";

    private static boolean sLoggedIn;
    private static Runnable sPendingSuccess;

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
        if (activity == null || !(activity instanceof FragmentActivity)) return;
        sPendingSuccess = onSuccess;
        Tools.swapFragment((FragmentActivity) activity, KollegenLoginFragment.class, KollegenLoginFragment.TAG, null);
    }

    public static Runnable takePendingSuccess() {
        Runnable result = sPendingSuccess;
        sPendingSuccess = null;
        return result;
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