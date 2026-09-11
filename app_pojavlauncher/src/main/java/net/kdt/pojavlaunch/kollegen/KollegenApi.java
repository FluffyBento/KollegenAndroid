package net.kdt.pojavlaunch.kollegen;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KollegenApi {
    public static final String BASE_URL = "https://kollegen.me";

    private static final ExecutorService sExecutor = Executors.newCachedThreadPool();
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private KollegenApi() {}

    public interface Callback {
        void onResult(Object json);
        void onError(String message);
    }

    public static void get(String path, Callback cb) {
        execute("GET", path, null, cb);
    }

    public static void post(String path, JSONObject body, Callback cb) {
        execute("POST", path, body, cb);
    }

    private static void execute(String method, String path, JSONObject body, Callback cb) {
        sExecutor.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                String cookie = KollegenSession.cookie();
                if (cookie != null && !cookie.isEmpty()) conn.setRequestProperty("Cookie", cookie);
                conn.setRequestProperty("Accept", "application/json");
                if (body != null) {
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);
                    try (OutputStream out = conn.getOutputStream()) {
                        out.write(data);
                    }
                }
                int code = conn.getResponseCode();
                InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                String text = in != null ? readAll(in) : "";
                Object parsed = parse(text);
                sHandler.post(() -> {
                    if (parsed != null && code >= 200 && code < 300) {
                        cb.onResult(parsed);
                    } else {
                        cb.onError(errorText(parsed, code));
                    }
                });
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                sHandler.post(() -> cb.onError(message));
            }
        });
    }

    private static Object parse(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return new JSONObject(text);
        } catch (Exception jsonObjectErr) {
            try {
                return new JSONArray(text);
            } catch (Exception jsonArrayErr) {
                return null;
            }
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }

    private static String errorText(Object parsed, int code) {
        if (parsed instanceof JSONObject) {
            String text = ((JSONObject) parsed).optString("error", null);
            if (text != null) return code + ": " + text;
        }
        return "HTTP " + code;
    }

    public static String optString(JSONObject obj, String key) {
        return obj != null && obj.has(key) && !obj.isNull(key) ? obj.optString(key) : "";
    }

    public static JSONArray optArray(Object json, String key) {
        if (json instanceof JSONArray) return (JSONArray) json;
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            return obj.has(key) && !obj.isNull(key) ? obj.optJSONArray(key) : null;
        }
        return null;
    }

    public static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }
}