package net.kdt.pojavlaunch;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KollegenTheme {
    public static final String PREF_KEY_THEME = "kollegen_theme";

    private static final Map<String, int[]> PALETTES = new LinkedHashMap<>();

    static {
        PALETTES.put("Kollegen", colors("#0a0c10", "#12141d", "#161922", "#ffaa00", "#f5c518", "#ededed", "#9ca3af", "#282d3d", "#ff5b6e"));
        PALETTES.put("Limit_Los", colors("#140a0a", "#1d0f0f", "#271414", "#FF0000", "#cc0000", "#f3e9e9", "#c39b9b", "#600000", "#ff5b6e"));
        PALETTES.put("FluffyBento", colors("#0d0912", "#160f1e", "#1e1524", "#b054d8", "#7c2fa3", "#f6ecfa", "#c2a8d4", "#332050", "#ff6b9d"));
        PALETTES.put("T_son_", colors("#0c1410", "#112019", "#16271e", "#2ecc71", "#239b56", "#e8f5ee", "#9bc2ac", "#244234", "#ff5b6e"));
        PALETTES.put("zSpicyyy", colors("#0a1218", "#0f1a22", "#14222c", "#3498db", "#2471a3", "#e6f1f8", "#9bbccc", "#223a48", "#ff7a59"));
        PALETTES.put("Irongirl", colors("#14171a", "#1c2024", "#24292e", "#bdc3c7", "#95a5a6", "#f0f3f5", "#aab4ba", "#2e343a", "#ff5b6e"));
        PALETTES.put("Zerocraft77", colors("#050505", "#0a0a0a", "#101010", "#b0b0b0", "#6e6e6e", "#e6e6e6", "#8a8a8a", "#1f1f1f", "#ff5252"));
        PALETTES.put("Erhaltunq", colors("#1c1610", "#241c14", "#2c2118", "#d4b482", "#a98c55", "#f3ecdf", "#c9b79a", "#3a2d1e", "#ff6b5e"));
    }

    private KollegenTheme() {}

    private static int[] colors(String bg, String panel, String panel2, String accent, String accent2, String text, String muted, String border, String danger) {
        return new int[]{Color.parseColor(bg), Color.parseColor(panel), Color.parseColor(panel2), Color.parseColor(accent), Color.parseColor(accent2), Color.parseColor(text), Color.parseColor(muted), Color.parseColor(border), Color.parseColor(danger)};
    }

    public static int BG = 0;
    public static int PANEL = 1;
    public static int PANEL2 = 2;
    public static int ACCENT = 3;
    public static int ACCENT2 = 4;
    public static int TEXT = 5;
    public static int MUTED = 6;
    public static int BORDER = 7;
    public static int DANGER = 8;

    public static String currentName() {
        String name = LauncherPreferences.DEFAULT_PREF.getString(PREF_KEY_THEME, "Kollegen");
        return PALETTES.containsKey(name) ? name : "Kollegen";
    }

    public static int[] palette() {
        return PALETTES.get(currentName());
    }

    public static int color(int index) {
        return palette()[index];
    }

    public static void applyWindow(Window window) {
        int[] pal = palette();
        window.setStatusBarColor(pal[KollegenTheme.BG]);
        window.setNavigationBarColor(pal[KollegenTheme.BG]);
        window.getDecorView().setBackgroundColor(pal[KollegenTheme.BG]);
    }

    public static void applyBackground(View view, int index) {
        if (view != null) view.setBackgroundColor(color(index));
    }

    public static StateListDrawable buttonBackground(int basePaletteIndex, int pressedIndex) {
        int[] pal = palette();
        GradientDrawable normal = rounded(pal[basePaletteIndex], pal[BORDER]);
        GradientDrawable pressed = rounded(pal[pressedIndex], pal[BORDER]);
        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(new int[]{android.R.attr.state_pressed}, pressed);
        stateList.addState(new int[]{}, normal);
        return stateList;
    }

    public static GradientDrawable rounded(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(28f);
        drawable.setColor(fill);
        drawable.setStroke(2, stroke);
        return drawable;
    }

    private static final int[] STATIC_COLORS = {
            Color.parseColor("#181818"),
            Color.parseColor("#242424"),
            Color.parseColor("#232323"),
            Color.parseColor("#464646"),
            Color.parseColor("#9649b8"),
            Color.parseColor("#131313"),
            Color.parseColor("#272727"),
            Color.parseColor("#909090"),
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#B2B2B2"),
    };

    private static final int[] STATIC_MAP = {
            BG,
            PANEL,
            PANEL2,
            PANEL2,
            ACCENT,
            PANEL,
            PANEL2,
            MUTED,
            TEXT,
            MUTED,
    };

    public static void applyTree(View view) {
        if (view == null) return;
        applyView(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTree(group.getChildAt(i));
            }
        }
    }

    public static void applyView(View view) {
        if (view == null) return;
        int[] pal = palette();

        if (view instanceof EditText) {
            EditText edit = (EditText) view;
            edit.setBackground(rounded(pal[PANEL], pal[BORDER]));
            edit.setTextColor(remapColor(edit.getTextColors().getDefaultColor(), TEXT));
            edit.setHintTextColor(remapColor(edit.getCurrentHintTextColor(), MUTED));
        } else if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextColor(remapColor(text.getTextColors().getDefaultColor(), TEXT));
            text.setHintTextColor(remapColor(text.getCurrentHintTextColor(), MUTED));
        }

        Drawable bg = view.getBackground();
        if (bg instanceof ColorDrawable) {
            int color = ((ColorDrawable) bg).getColor();
            for (int i = 0; i < STATIC_COLORS.length; i++) {
                if (color == STATIC_COLORS[i]) {
                    view.setBackgroundColor(pal[STATIC_MAP[i]]);
                    break;
                }
            }
        }
    }

    private static int remapColor(int color, int fallback) {
        for (int i = 0; i < STATIC_COLORS.length; i++) {
            if (color == STATIC_COLORS[i]) return palette()[STATIC_MAP[i]];
        }
        return fallback;
    }
}