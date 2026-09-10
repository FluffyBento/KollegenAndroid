package net.kdt.pojavlaunch.kollegen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KollegenAvatar {
    private static final ExecutorService sExecutor = Executors.newCachedThreadPool();
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private KollegenAvatar() {}

    public static void loadMcHead(ImageView view, String mcName) {
        sExecutor.execute(() -> {
            Bitmap bmp = download("https://mc-heads.net/avatar/" + encodeName(mcName) + "/96");
            sHandler.post(() -> {
                if (bmp != null) {
                    view.setImageBitmap(bmp);
                    view.setBackground(null);
                }
            });
        });
    }

    public static void loadDataUrl(ImageView view, String dataUrl) {
        sExecutor.execute(() -> {
            Bitmap bmp = decodeDataUrl(dataUrl);
            sHandler.post(() -> {
                if (bmp != null) {
                    view.setImageBitmap(bmp);
                    view.setBackground(null);
                }
            });
        });
    }

    private static String encodeName(String name) {
        return name.replace(" ", "_");
    }

    private static Bitmap decodeDataUrl(String dataUrl) {
        try {
            int comma = dataUrl.indexOf(',');
            if (comma < 0) return null;
            String prefix = dataUrl.substring(0, comma);
            String payload = dataUrl.substring(comma + 1);
            byte[] bytes;
            if (prefix.contains("base64")) {
                bytes = Base64.decode(payload, Base64.DEFAULT);
            } else {
                bytes = payload.getBytes("UTF-8");
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap download(String urlString) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Accept", "image/*");
            if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) return null;
            InputStream in = conn.getInputStream();
            byte[] bytes = readAll(in);
            in.close();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }
}