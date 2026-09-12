package net.kdt.pojavlaunch.kollegen;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KollegenKit {
    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-fA-F]{6,8}");
    private static JSONArray sCatalog;
    private static boolean sCatalogLoading;

    private KollegenKit() {}

    public static void ensureCatalog(final Runnable onReady) {
        if (sCatalog != null) {
            if (onReady != null) onReady.run();
            return;
        }
        if (sCatalogLoading) return;
        sCatalogLoading = true;
        KollegenApi.get("/api/profil/store", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                sCatalogLoading = false;
                if (json instanceof JSONObject) {
                    sCatalog = ((JSONObject) json).optJSONArray("catalog");
                }
                if (onReady != null) onReady.run();
            }

            @Override
            public void onError(String message) {
                sCatalogLoading = false;
                if (onReady != null) onReady.run();
            }
        });
    }

    public static JSONObject catalogItem(String id) {
        if (sCatalog == null || id == null || id.isEmpty()) return null;
        for (int i = 0; i < sCatalog.length(); i++) {
            JSONObject it = sCatalog.optJSONObject(i);
            if (it != null && id.equals(it.optString("id"))) return it;
        }
        return null;
    }

    public static JSONArray catalog() {
        return sCatalog;
    }

    public static JSONObject resolveEquippedTree(JSONObject user) {
        JSONObject res = new JSONObject();
        JSONObject eq = user != null ? user.optJSONObject("equipped") : null;
        if (eq == null) return res;
        String[] cats = {"title", "badge", "avatar_theme", "avatar_frame", "profile_bg", "profile_frame", "banner", "sticker", "name_color", "font", "profil_stil"};
        for (String c : cats) {
            Object v = eq.opt(c);
            if (v instanceof JSONObject) {
                JSONObject jo = (JSONObject) v;
                if (jo.has("data")) {
                    try { res.put(c, jo); } catch (Exception ignored) {}
                    continue;
                }
                String id = jo.optString("id", "");
                JSONObject resolved = catalogItem(id);
                if (resolved != null) {
                    try { res.put(c, resolved); } catch (Exception ignored) {}
                }
            } else if (v instanceof String) {
                JSONObject resolved = catalogItem((String) v);
                if (resolved != null) {
                    try { res.put(c, resolved); } catch (Exception ignored) {}
                }
            }
        }
        return res;
    }

    public static int accent(JSONObject eqResolved, int fallback) {
        if (eqResolved != null) {
            JSONObject nc = eqResolved.optJSONObject("name_color");
            if (nc != null) {
                JSONObject d = nc.optJSONObject("data");
                String a = d != null ? d.optString("accent", "") : "";
                int c = parseColor(a, -1);
                if (c != -1) return c;
            }
            JSONObject stil = eqResolved.optJSONObject("profil_stil");
            if (stil != null) {
                JSONObject d = stil.optJSONObject("data");
                String a = d != null ? d.optString("accent", "") : "";
                int c = parseColor(a, -1);
                if (c != -1) return c;
            }
        }
        return fallback;
    }

    public static String applyTitle(JSONObject eqResolved, String name) {
        JSONObject t = eqResolved != null ? eqResolved.optJSONObject("title") : null;
        if (t != null) {
            JSONObject d = t.optJSONObject("data");
            String text = d != null ? d.optString("text", "") : "";
            if (!text.isEmpty()) return name + " \u00b7 " + text;
        }
        return name;
    }

    public static String equippedName(JSONObject eqResolved, String category) {
        JSONObject it = eqResolved != null ? eqResolved.optJSONObject(category) : null;
        return it != null ? it.optString("name", "") : "";
    }

    public static int parseColor(String hex, int fallback) {
        if (hex == null) return fallback;
        try {
            String h = hex.trim();
            if (h.startsWith("#")) h = h.substring(1);
            if (h.startsWith("0x") || h.startsWith("0X")) h = h.substring(2);
            if (h.length() == 6) h = "FF" + h;
            if (h.length() == 8) return (int) Long.parseLong(h, 16);
        } catch (Exception ignored) {}
        return fallback;
    }

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable gradient(Context c, String spec, int fallback) {
        int c1 = fallback;
        int c2 = fallback;
        if (spec != null && spec.contains("#")) {
            Matcher m = HEX_COLOR.matcher(spec);
            List<Integer> colors = new ArrayList<>();
            while (m.find()) {
                int col = parseColor(m.group(), -1);
                if (col != -1) colors.add(col);
            }
            if (colors.size() >= 1) c1 = colors.get(0);
            if (colors.size() >= 2) c2 = colors.get(colors.size() - 1);
        }
        GradientDrawable.Orientation o = GradientDrawable.Orientation.TL_BR;
        if (spec != null && (spec.contains("to left") || spec.contains("to right"))) o = GradientDrawable.Orientation.LEFT_RIGHT;
        else if (spec != null && spec.contains("to top")) o = GradientDrawable.Orientation.BOTTOM_TOP;
        GradientDrawable gd = new GradientDrawable(o, new int[]{c1, c2});
        gd.setCornerRadius(dp(c, 8));
        return gd;
    }

    public static GradientDrawable roundedBorder(Context c, String hex, int w, int fill, int fallback) {
        int col = parseColor(hex, fallback);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(dp(c, 10));
        gd.setColor(fill);
        gd.setStroke(w, col);
        return gd;
    }

    public static View bannerView(Context c, JSONObject data, int[] pal) {
        LinearLayout banner = new LinearLayout(c);
        banner.setOrientation(LinearLayout.VERTICAL);
        banner.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 48));
        lp.setMargins(0, dp(c, 8), 0, 0);
        banner.setLayoutParams(lp);
        String grad = data != null ? data.optString("gradient", "") : "";
        banner.setBackground(gradient(c, grad, pal[KollegenTheme.ACCENT]));
        TextView t = new TextView(c);
        t.setText("Banner");
        t.setTextColor(Color.parseColor("#0a0d13"));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        banner.addView(t);
        return banner;
    }

    public static boolean renderEquippedRow(Context c, LinearLayout row, JSONObject eqResolved, int[] pal, int size) {
        boolean any = false;
        if (eqResolved != null) {
            String[] order = {"badge", "avatar_frame", "avatar_theme", "profile_bg", "profile_frame", "profil_stil", "banner", "font"};
            for (String cat : order) {
                final JSONObject it = eqResolved.optJSONObject(cat);
                if (it == null) continue;
                any = true;
                View pv = storePreview(c, it, size);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMargins(0, 0, dp(c, 8), 0);
                pv.setLayoutParams(lp);
                row.addView(pv);
            }
        }
        return any;
    }

    public static View storePreview(Context c, JSONObject item, int size) {
        String category = item.optString("category", "");
        JSONObject data = item.optJSONObject("data");
        int[] pal = KollegenTheme.palette();
        int aColor = data != null ? parseColor(data.optString("accent", data.optString("color1", "")), pal[KollegenTheme.ACCENT]) : pal[KollegenTheme.ACCENT];

        FrameLayout box = new FrameLayout(c);
        box.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        box.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL2], pal[KollegenTheme.BORDER]));

        if ("title".equals(category)) {
            TextView t = new TextView(c);
            String txt = data != null ? data.optString("text", "") : "";
            t.setText(txt.isEmpty() ? "Titel" : txt);
            t.setTextSize(11);
            t.setTextColor(pal[KollegenTheme.TEXT]);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setGravity(Gravity.CENTER);
            box.addView(t);
        } else if ("badge".equals(category)) {
            TextView t = new TextView(c);
            String icon = data != null ? data.optString("icon", "\u2605") : "\u2605";
            t.setText(icon);
            t.setTextSize(24);
            t.setTextColor(data != null ? parseColor(data.optString("color", ""), aColor) : aColor);
            t.setGravity(Gravity.CENTER);
            box.addView(t);
        } else if ("avatar_theme".equals(category)) {
            box.setBackground(KollegenKit.gradient(c, data != null ? data.optString("gradient", "") : "", aColor));
            ImageView av = new ImageView(c);
            FrameLayout.LayoutParams avLp = new FrameLayout.LayoutParams(dp(c, 24), dp(c, 24), Gravity.CENTER);
            av.setLayoutParams(avLp);
            KollegenAvatar.loadMcHead(av, "MHF_Steve");
            box.addView(av);
        } else if ("banner".equals(category)) {
            box.setBackground(KollegenKit.gradient(c, data != null ? data.optString("gradient", "") : "", aColor));
            TextView t = new TextView(c);
            t.setText("Banner");
            t.setTextColor(Color.parseColor("#0a0d13"));
            t.setTextSize(10);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setGravity(Gravity.CENTER);
            box.addView(t);
        } else if ("profile_bg".equals(category)) {
            box.setBackground(KollegenKit.gradient(c, data != null ? data.optString("gradient", "") : "", aColor));
            ImageView av = new ImageView(c);
            FrameLayout.LayoutParams avLp = new FrameLayout.LayoutParams(dp(c, 22), dp(c, 22), Gravity.CENTER);
            av.setLayoutParams(avLp);
            KollegenAvatar.loadMcHead(av, "MHF_Steve");
            box.addView(av);
        } else if ("profile_frame".equals(category)) {
            box.setBackground(KollegenKit.roundedBorder(c, data != null ? data.optString("color1", "") : "", 3, pal[KollegenTheme.PANEL2], aColor));
            ImageView av = new ImageView(c);
            FrameLayout.LayoutParams avLp = new FrameLayout.LayoutParams(dp(c, 30), dp(c, 30), Gravity.CENTER);
            av.setLayoutParams(avLp);
            KollegenAvatar.loadMcHead(av, "MHF_Steve");
            box.addView(av);
        } else if ("profil_stil".equals(category)) {
            FrameLayout mini = new FrameLayout(c);
            FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(dp(c, 40), dp(c, 40), Gravity.CENTER);
            mini.setLayoutParams(mlp);
            mini.setBackground(KollegenKit.roundedBorder(c, data != null ? data.optString("accent", "") : "", 2, pal[KollegenTheme.PANEL2], aColor));
            TextView t = new TextView(c);
            t.setText("Aa");
            t.setTextSize(15);
            t.setTextColor(aColor);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setGravity(Gravity.CENTER);
            mini.addView(t);
            box.addView(mini);
        } else if ("font".equals(category)) {
            TextView t = new TextView(c);
            String f = data != null ? data.optString("font", "") : "";
            t.setText(f.isEmpty() ? "Aa" : f);
            t.setTextSize(11);
            t.setTextColor(pal[KollegenTheme.MUTED]);
            t.setGravity(Gravity.CENTER);
            box.addView(t);
        } else if ("sticker".equals(category)) {
            TextView t = new TextView(c);
            String icon = data != null ? data.optString("icon", "\u2728") : "\u2728";
            t.setText(icon);
            t.setTextSize(24);
            t.setTextColor(aColor);
            t.setGravity(Gravity.CENTER);
            box.addView(t);
        } else {
            ImageView av = new ImageView(c);
            FrameLayout.LayoutParams avLp = new FrameLayout.LayoutParams(dp(c, 36), dp(c, 36), Gravity.CENTER);
            av.setLayoutParams(avLp);
            KollegenAvatar.loadMcHead(av, "MHF_Steve");
            box.addView(av);
            if ("avatar_frame".equals(category)) {
                String c1 = data != null ? data.optString("color1", "") : "";
                int w = data != null ? data.optInt("width", 3) : 3;
                box.setBackground(KollegenKit.roundedBorder(c, c1, w, pal[KollegenTheme.PANEL2], aColor));
            }
        }
        return box;
    }

    public static GradientDrawable avatarBackground(Context c, JSONObject eqResolved, int[] pal) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(dp(c, 22));
        JSONObject theme = eqResolved != null ? eqResolved.optJSONObject("avatar_theme") : null;
        String grad = "";
        String c1 = "";
        String c2 = "";
        if (theme != null) {
            JSONObject d = theme.optJSONObject("data");
            if (d != null) {
                grad = d.optString("gradient", "");
                c1 = d.optString("color1", "");
                c2 = d.optString("color2", c1);
            }
        }
        if (!grad.isEmpty()) {
            gd = gradient(c, grad, pal[KollegenTheme.PANEL2]);
            gd.setCornerRadius(dp(c, 22));
        } else if (!c1.isEmpty()) {
            int a = parseColor(c1, pal[KollegenTheme.PANEL2]);
            int b = parseColor(c2, a);
            gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a, b});
            gd.setCornerRadius(dp(c, 22));
        } else {
            gd.setColor(pal[KollegenTheme.PANEL2]);
        }
        JSONObject frame = eqResolved != null ? eqResolved.optJSONObject("avatar_frame") : null;
        String f1 = "";
        int fw = 4;
        if (frame != null) {
            JSONObject d = frame.optJSONObject("data");
            if (d != null) {
                f1 = d.optString("color1", "");
                fw = Math.max(2, d.optInt("width", 4));
            }
        }
        if (!f1.isEmpty()) {
            gd.setStroke(dp(c, fw), parseColor(f1, pal[KollegenTheme.ACCENT]));
        }
        return gd;
    }

    public static FrameLayout avatarHolder(Context c, JSONObject eqResolved, int sizeDp, int[] pal) {
        int size = dp(c, sizeDp);
        FrameLayout holder = new FrameLayout(c);
        holder.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        holder.setBackground(avatarBackground(c, eqResolved, pal));
        ImageView av = new ImageView(c);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, Gravity.CENTER);
        av.setLayoutParams(lp);
        av.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.addView(av);
        return holder;
    }

    public static View bannerImage(Context c, String dataUrl, int[] pal) {
        FrameLayout b = new FrameLayout(c);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 48));
        lp.setMargins(0, dp(c, 8), 0, 0);
        b.setLayoutParams(lp);
        b.setBackground(gradient(c, "", pal[KollegenTheme.PANEL2]));
        if (dataUrl != null && !dataUrl.isEmpty()) {
            ImageView iv = new ImageView(c);
            iv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            KollegenAvatar.loadImage(iv, dataUrl);
            b.addView(iv);
        }
        TextView t = new TextView(c);
        t.setText("Banner");
        t.setTextColor(Color.parseColor("#0a0d13"));
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        b.addView(t);
        return b;
    }

    public static String badgeIcon(JSONObject eqResolved) {
        JSONObject it = eqResolved != null ? eqResolved.optJSONObject("badge") : null;
        JSONObject d = it != null ? it.optJSONObject("data") : null;
        return d != null ? d.optString("icon", "") : "";
    }

    public static int badgeColor(JSONObject eqResolved, int fallback) {
        JSONObject it = eqResolved != null ? eqResolved.optJSONObject("badge") : null;
        JSONObject d = it != null ? it.optJSONObject("data") : null;
        return d != null ? parseColor(d.optString("color", ""), fallback) : fallback;
    }

    public static String stickerIcon(JSONObject eqResolved) {
        JSONObject it = eqResolved != null ? eqResolved.optJSONObject("sticker") : null;
        JSONObject d = it != null ? it.optJSONObject("data") : null;
        return d != null ? d.optString("icon", "") : "";
    }

    public static int stickerColor(JSONObject eqResolved, int fallback) {
        JSONObject it = eqResolved != null ? eqResolved.optJSONObject("sticker") : null;
        JSONObject d = it != null ? it.optJSONObject("data") : null;
        return d != null ? parseColor(d.optString("color", ""), fallback) : fallback;
    }

    public static String nameFont(JSONObject eqResolved) {
        JSONObject it = eqResolved != null ? eqResolved.optJSONObject("font") : null;
        JSONObject d = it != null ? it.optJSONObject("data") : null;
        return d != null ? d.optString("font", "") : "";
    }

    public static void applyNameFont(TextView t, String font) {
        if (font == null || font.isEmpty()) return;
        try {
            String f = font.toLowerCase().trim();
            if (f.contains("serif")) t.setTypeface(Typeface.SERIF);
            else if (f.contains("mono")) t.setTypeface(Typeface.MONOSPACE);
            else if (f.contains("sans") || f.contains("sans-serif")) t.setTypeface(Typeface.SANS_SERIF);
        } catch (Exception ignored) {}
    }

    public static void renderInventory(Context c, LinearLayout box, JSONObject eqResolved, JSONArray owned, int[] pal, int sizeDp, String emptyText) {
        List<JSONObject> entries = new ArrayList<>();
        if (owned != null) {
            for (int i = 0; i < owned.length(); i++) {
                JSONObject o = owned.optJSONObject(i);
                if (o == null) continue;
                String id = o.optString("id", "");
                String cat = o.optString("category", "");
                JSONObject ci = catalogItem(id);
                if (ci == null) continue;
                JSONObject wrap = new JSONObject();
                try {
                    wrap.put("id", id);
                    wrap.put("category", ci.optString("category", cat));
                    wrap.put("name", ci.optString("name", id));
                    wrap.put("data", ci.optJSONObject("data"));
                } catch (Exception ignored) {}
                entries.add(wrap);
            }
        }
        if (entries.isEmpty()) {
            TextView t = new TextView(c);
            t.setText(emptyText);
            t.setTextColor(pal[KollegenTheme.MUTED]);
            t.setTextSize(13);
            t.setPadding(0, dp(c, 8), 0, 0);
            box.addView(t);
            return;
        }
        LinearLayout strip = new LinearLayout(c);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setPadding(0, dp(c, 8), 0, 0);
        box.addView(strip);
        int perRow = Math.max(4, c.getResources().getDisplayMetrics().widthPixels / (dp(c, sizeDp + 10)));
        int count = 0;
        for (JSONObject it : entries) {
            if (count > 0 && count % perRow == 0) {
                strip = new LinearLayout(c);
                strip.setOrientation(LinearLayout.HORIZONTAL);
                strip.setPadding(0, dp(c, 8), 0, 0);
                box.addView(strip);
            }
            View pv = storePreview(c, it, dp(c, sizeDp));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp));
            lp.setMargins(0, 0, dp(c, 6), 0);
            pv.setLayoutParams(lp);
            strip.addView(pv);
            count++;
        }
    }

    public static Button tinyButton(Context c, String text) {
        int[] pal = KollegenTheme.palette();
        Button b = new Button(c);
        b.setText(text);
        b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(pal[KollegenTheme.TEXT]);
        b.setAllCaps(false);
        b.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL2], pal[KollegenTheme.BORDER]));
        b.setPadding(dp(c, 8), dp(c, 4), dp(c, 8), dp(c, 4));
        return b;
    }

    public static TextView empty(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextColor(KollegenTheme.palette()[KollegenTheme.MUTED]);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(c, 20), 0, dp(c, 20));
        return t;
    }
}