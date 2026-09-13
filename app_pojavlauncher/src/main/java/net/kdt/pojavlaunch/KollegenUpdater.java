package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KollegenUpdater {
    private static final String REPO = "FluffyBento/KollegenAndroid";
    private static final String LATEST_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String PREFS = "kollegen_update";
    private static final String KEY_LAST_CHECK = "last_check";
    private static final long CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000;
    private static final Pattern VERSION_PATTERN = Pattern.compile("v?(\\d+)\\.(\\d+)\\.(\\d+)");

    private KollegenUpdater() {}

    public static void checkIfDue(final Activity activity) {
        if (activity == null) return;
        final long now = System.currentTimeMillis();
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, 0);
        if (now - prefs.getLong(KEY_LAST_CHECK, 0) < CHECK_INTERVAL_MS) return;
        prefs.edit().putLong(KEY_LAST_CHECK, now).apply();
        performCheck(activity, false);
    }

    public static void checkNow(final Activity activity) {
        if (activity == null) return;
        performCheck(activity, true);
    }

    private static void performCheck(final Activity activity, final boolean notifyResult) {
        Thread thread = new Thread(() -> {
            try {
                final UpdateInfo update = fetchLatest();
                if (update == null) {
                    if (notifyResult) new Handler(Looper.getMainLooper()).post(() -> toast(activity, R.string.koll_settings_update_error));
                    return;
                }
                final int[] current = parseVersion(BuildConfig.VERSION_NAME);
                final int[] latest = parseVersion(update.tag);
                if (current == null || latest == null || !isNewer(latest, current)) {
                    if (notifyResult) new Handler(Looper.getMainLooper()).post(() -> toast(activity, R.string.koll_settings_update_uptodate));
                    return;
                }
                new Handler(Looper.getMainLooper()).post(() -> showPrompt(activity, update));
            } catch (Exception ignored) {
            }
        }, "kollegen-update-check");
        thread.start();
    }

    private static void toast(Activity activity, int resId) {
        Toast.makeText(activity, activity.getString(resId), Toast.LENGTH_LONG).show();
    }

    private static UpdateInfo fetchLatest() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(LATEST_URL).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("User-Agent", Tools.APP_NAME);
        int code = conn.getResponseCode();
        if (code != 200) return null;
        try (InputStream in = conn.getInputStream()) {
            byte[] buf = new byte[8192];
            StringBuilder sb = new StringBuilder();
            int n;
            while ((n = in.read(buf)) != -1) sb.append(new String(buf, 0, n, java.nio.charset.StandardCharsets.UTF_8));
            JSONObject root = new JSONObject(sb.toString());
            String tag = root.optString("tag_name");
            String assetUrl = findApk(root.optJSONArray("assets"));
            if (tag.isEmpty() || assetUrl == null) return null;
            return new UpdateInfo(tag, assetUrl);
        }
    }

    private static String findApk(JSONArray assets) {
        if (assets == null) return null;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null) continue;
            String name = asset.optString("name");
            if (name != null && name.endsWith(".apk")) {
                return asset.optString("browser_download_url");
            }
        }
        return null;
    }

    private static void showPrompt(final Activity activity, final UpdateInfo update) {
        if (activity == null || activity.isDestroyed()) return;
        try {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.kollegen_update_title)
                    .setMessage(activity.getString(R.string.kollegen_update_message, update.tag))
                    .setPositiveButton(R.string.kollegen_update_install, (d, w) -> downloadAndInstall(activity, update))
                    .setNegativeButton(R.string.kollegen_update_cancel, null)
                    .show();
        } catch (Exception ignored) {
        }
    }

    private static void downloadAndInstall(final Activity activity, final UpdateInfo update) {
        if (activity == null || activity.isDestroyed()) return;
        final android.app.ProgressDialog progress = new android.app.ProgressDialog(activity);
        progress.setMessage(activity.getString(R.string.kollegen_update_downloading));
        progress.setCancelable(false);
        progress.show();

        Thread thread = new Thread(() -> {
            final Exception[] error = {null};
            try {
                File out = new File(Tools.DIR_CACHE, "kollegen-update.apk");
                if (out.isFile()) out.delete();
                DownloadUtils.downloadFile(update.apkUrl, out);
                if (!out.isFile() || out.length() == 0) throw new IOException("Empty download");
            } catch (Exception e) {
                error[0] = e;
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                if (activity.isDestroyed()) return;
                if (progress.isShowing()) progress.dismiss();
                if (error[0] != null) {
                    Toast.makeText(activity, activity.getString(R.string.kollegen_update_error, String.valueOf(error[0].getMessage())), Toast.LENGTH_LONG).show();
                    return;
                }
                install(activity);
            });
        }, "kollegen-update-download");
        thread.start();
    }

    private static void install(Activity activity) {
        try {
            File apk = new File(Tools.DIR_CACHE, "kollegen-update.apk");
            Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", apk);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, activity.getString(R.string.kollegen_update_error, String.valueOf(e.getMessage())), Toast.LENGTH_LONG).show();
        }
    }

    static int[] parseVersion(String value) {
        if (value == null) return null;
        Matcher m = VERSION_PATTERN.matcher(value);
        if (!m.find()) return null;
        return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))};
    }

    private static boolean isNewer(int[] latest, int[] current) {
        for (int i = 0; i < 3; i++) {
            if (latest[i] > current[i]) return true;
            if (latest[i] < current[i]) return false;
        }
        return false;
    }

    private static class UpdateInfo {
        final String tag;
        final String apkUrl;

        UpdateInfo(String tag, String apkUrl) {
            this.tag = tag;
            this.apkUrl = apkUrl;
        }
    }
}