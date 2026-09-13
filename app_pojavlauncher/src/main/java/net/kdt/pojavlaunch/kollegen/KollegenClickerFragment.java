package net.kdt.pojavlaunch.kollegen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pl.droidsonroids.gif.GifDrawable;

public class KollegenClickerFragment extends Fragment {
    private static final String SYNC_URL = "/api/clicker/sync";
    private static final String STATE_URL = "/api/clicker/state";
    private static final String CONFIG_URL = "/api/clicker/config";
    private static final String HAPPI_HIT_URL = "/api/clicker/event/happi-hit";
    private static final String EVENT_STREAM_URL = "/api/clicker/leaderboard/stream";
    private static final String HAPPI_IMAGE = "/images/skins/low.gif";
    private static final String INVASION_IMAGE = "/images/skins/high.png";
    private static final long SYNC_INTERVAL = 15000;
    private static final long CPS_TICK = 1000;
    private static final long COMBO_WINDOW = 1600;
    private static final double CRIT_CHANCE = 0.00001;

    private static final int C_BG = Color.parseColor("#08090C");
    private static final int C_PANEL = Color.parseColor("#12141D");
    private static final int C_STONE = Color.parseColor("#1C1F2B");
    private static final int C_GOLD = Color.parseColor("#FFAA00");
    private static final int C_GOLD_DARK = Color.parseColor("#241708");
    private static final int C_GREEN = Color.parseColor("#55FF55");
    private static final int C_TEXT = Color.parseColor("#FFFFFF");
    private static final int C_MUTED = Color.parseColor("#71717A");
    private static final int C_PROGRESS = Color.parseColor("#1A1D2A");
    private static final int C_RAGE = Color.parseColor("#CC7F1D1D");

    private long mClicks;
    private long mTotalClicks;
    private final Map<String, Integer> mUpgrades = new HashMap<>();
    private final Set<String> mUnlockedAchievements = new HashSet<>();
    private final Set<String> mClaimedAchievements = new HashSet<>();
    private final Set<String> mUnlockedSkins = new HashSet<>();
    private String mEquippedSkin = "default";
    private int mComboCount;
    private long mLastClickTime;
    private long mCritUntil;
    private boolean mSyncing;
    private volatile boolean mRunning;
    private int mCurrentTab;

    private JSONArray mUpgradeConfig;
    private JSONArray mAchievementConfig;
    private JSONArray mSkinConfig;

    private TextView mLevel;
    private TextView mLevelSub;
    private View mProgressBar;
    private TextView mCount;
    private TextView mBalance;
    private TextView mClickVal;
    private TextView mCps;
    private ImageView mClickButton;
    private Button mSkinPill;
    private LinearLayout mContent;
    private Button mTabUpgrades;
    private Button mTabSkins;
    private Button mTabAchievements;
    private FrameLayout mOverlay;

    private TextView mBanner;
    private Runnable mBannerHide;
    private LinearLayout mComboChip;
    private TextView mComboChipT1;
    private TextView mComboChipT2;
    private Runnable mComboHide;
    private TextView mRageChip;
    private long mRageUntil;
    private final Runnable mRageTick = new Runnable() {
        @Override
        public void run() {
            if (getView() == null || mOverlay == null) return;
            if (mRageUntil > System.currentTimeMillis()) {
                if (mRageChip != null) {
                    float left = (mRageUntil - System.currentTimeMillis()) / 1000f;
                    mRageChip.setText("🔥 RAGE MODUS: +50% MEHR KLICKS & CPS (" + String.format(Locale.GERMANY, "%.1f", left) + "s)! 🔥");
                }
                mHandler.postDelayed(this, 100);
            } else {
                removeRage();
            }
        }
    };

    private View mBossOverlay;
    private ImageView mBossImage;
    private View mBossHpFill;
    private TextView mBossHpText;
    private boolean mBossOpen;
    private int mBossHp;
    private int mBossMaxHp;
    private long mLastHappiHit;

    private final List<View> mInvasionViews = new ArrayList<>();
    private final List<float[]> mInvasionVels = new ArrayList<>();
    private final Runnable mInvasionTick = new Runnable() {
        @Override
        public void run() {
            if (getView() == null || mOverlay == null) return;
            int w = getView().getWidth();
            int h = getView().getHeight();
            if (w <= 0 || h <= 0 || mInvasionViews.isEmpty()) return;
            boolean alive = false;
            for (int i = 0; i < mInvasionVels.size(); i++) {
                float[] v = mInvasionVels.get(i);
                v[0] += v[2];
                v[1] += v[3];
                float maxX = w - v[4] - dp(6);
                float maxY = h - v[4] - dp(6);
                if (v[0] <= dp(2)) { v[0] = dp(2); v[2] = Math.abs(v[2]); }
                if (v[0] >= maxX) { v[0] = maxX; v[2] = -Math.abs(v[2]); }
                if (v[1] <= dp(2)) { v[1] = dp(2); v[3] = Math.abs(v[3]); }
                if (v[1] >= maxY) { v[1] = maxY; v[3] = -Math.abs(v[3]); }
                View iv = mInvasionViews.get(i);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) iv.getLayoutParams();
                lp.leftMargin = (int) v[0];
                lp.topMargin = (int) v[1];
                iv.setLayoutParams(lp);
                alive = true;
            }
            if (alive) mHandler.postDelayed(this, 40);
        }
    };
    private Runnable mInvasionEnd;

    private Thread mSseThread;
    private volatile boolean mSseStop;
    private volatile HttpURLConnection mSseConn;

    private final NumberFormat mNf = NumberFormat.getNumberInstance(Locale.GERMANY);
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mCpsTick = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            int cps = calcCps();
            if (cps > 0) {
                mClicks += cps;
                mTotalClicks += cps;
            }
            updateStats();
            mHandler.postDelayed(this, CPS_TICK);
        }
    };
    private final Runnable mSyncRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            syncToServer();
            mHandler.postDelayed(this, SYNC_INTERVAL);
        }
    };

    public KollegenClickerFragment() {
        super(R.layout.fragment_kollegen_clicker);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mLevel = view.findViewById(R.id.koll_clicker_level);
        mLevelSub = view.findViewById(R.id.koll_clicker_level_sub);
        mProgressBar = view.findViewById(R.id.koll_clicker_progress_bar);
        mCount = view.findViewById(R.id.koll_clicker_count);
        mBalance = view.findViewById(R.id.koll_clicker_stat_balance);
        mClickVal = view.findViewById(R.id.koll_clicker_stat_clickval);
        mCps = view.findViewById(R.id.koll_clicker_stat_cps);
        mClickButton = view.findViewById(R.id.koll_clicker_button);
        mSkinPill = view.findViewById(R.id.koll_clicker_skin_pill);
        mContent = view.findViewById(R.id.koll_clicker_content);
        mTabUpgrades = view.findViewById(R.id.koll_clicker_tab_upgrades);
        mTabSkins = view.findViewById(R.id.koll_clicker_tab_skins);
        mTabAchievements = view.findViewById(R.id.koll_clicker_tab_achievements);
        mOverlay = view.findViewById(R.id.koll_clicker_overlay);

        mClickButton.setOnTouchListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                pressScale(v, 0.92f);
                handleClick(ev.getRawX(), ev.getRawY());
            } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                pressScale(v, 1f);
            }
            return true;
        });
        mSkinPill.setOnClickListener(v -> showTab(2));
        mTabUpgrades.setOnClickListener(v -> showTab(0));
        mTabSkins.setOnClickListener(v -> showTab(2));
        mTabAchievements.setOnClickListener(v -> showTab(1));
        applyTheme(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRunning = true;
        loadConfig();
        startEventStream();
        mHandler.postDelayed(mCpsTick, CPS_TICK);
        mHandler.postDelayed(mSyncRunnable, SYNC_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunning = false;
        mHandler.removeCallbacks(mCpsTick);
        mHandler.removeCallbacks(mSyncRunnable);
        removeCallbacksAll();
        stopEventStream();
        closeBoss();
        clearInvasion();
        removeRage();
        hideBanner();
        hideCombo();
        syncToServer();
    }

    private void removeCallbacksAll() {
        mHandler.removeCallbacks(mCpsTick);
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.removeCallbacks(mRageTick);
        mHandler.removeCallbacks(mBannerHide);
        mHandler.removeCallbacks(mComboHide);
        mHandler.removeCallbacks(mInvasionTick);
        mHandler.removeCallbacks(mInvasionEnd);
    }

    private void loadConfig() {
        KollegenApi.get(CONFIG_URL, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded() || !(json instanceof JSONObject)) return;
                JSONObject cfg = (JSONObject) json;
                mUpgradeConfig = cfg.optJSONArray("upgrades");
                mAchievementConfig = cfg.optJSONArray("achievements");
                mSkinConfig = cfg.optJSONArray("skins");
                mUnlockedSkins.add("default");
                loadState();
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadState() {
        KollegenApi.get(STATE_URL, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded() || !(json instanceof JSONObject)) return;
                JSONObject obj = (JSONObject) json;
                JSONObject state = obj.optJSONObject("state");
                if (state == null) {
                    updateStats();
                    reloadSkinImage();
                    showTab(0);
                    return;
                }
                mClicks = (long) state.optDouble("clicks", 0);
                mTotalClicks = (long) state.optDouble("totalClicks", 0);
                JSONObject su = state.optJSONObject("upgrades");
                if (su != null) {
                    Iterator<String> keys = su.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        mUpgrades.put(k, su.optInt(k, 0));
                    }
                }
                JSONArray ua = state.optJSONArray("unlockedAchievements");
                if (ua != null) {
                    for (int i = 0; i < ua.length(); i++) mUnlockedAchievements.add(ua.optString(i));
                }
                JSONArray ca = state.optJSONArray("claimedAchievements");
                if (ca != null) {
                    for (int i = 0; i < ca.length(); i++) mClaimedAchievements.add(ca.optString(i));
                }
                JSONArray us = state.optJSONArray("unlockedSkins");
                if (us != null) {
                    for (int i = 0; i < us.length(); i++) mUnlockedSkins.add(us.optString(i));
                }
                String es = state.optString("equippedSkin", "");
                if (!es.isEmpty()) mEquippedSkin = es;
                updateStats();
                reloadSkinImage();
                showTab(0);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                updateStats();
                showTab(0);
            }
        });
    }

    private void handleClick(float rawX, float rawY) {
        long now = System.currentTimeMillis();
        if (now - mLastClickTime > COMBO_WINDOW) mComboCount = 0;
        mLastClickTime = now;
        mComboCount++;
        int cpc = calcCpc();
        boolean crit = mCritUntil > now;
        mClicks += cpc;
        mTotalClicks += cpc;
        if (Math.random() < CRIT_CHANCE && mCritUntil <= now) {
            mCritUntil = now + 3000;
            showBanner("💥 KRITISCHER TREFFER! 100x COOKIES FÜR 3s! ⚡", 4000);
        }
        int[] loc = new int[2];
        if (mOverlay != null) {
            mOverlay.getLocationOnScreen(loc);
            float ox = rawX - loc[0];
            float oy = rawY - loc[1] - dp(14);
            floatTextAt("+" + mNf.format((long) cpc) + (crit ? " (100x CRIT!)" : ""), ox, oy, crit ? C_GOLD : C_TEXT, 14);
        }
        updateStats();
        popCount();
        updateComboUi();
        checkAchievements();
    }

    private void pressScale(View v, float scale) {
        ObjectAnimator a = ObjectAnimator.ofFloat(v, "scaleX", scale);
        a.setDuration(60);
        a.start();
        ObjectAnimator b = ObjectAnimator.ofFloat(v, "scaleY", scale);
        b.setDuration(60);
        b.start();
    }

    private void popCount() {
        if (mCount == null) return;
        ObjectAnimator sx = ObjectAnimator.ofFloat(mCount, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(mCount, "scaleY", 1f, 1.1f, 1f);
        sx.setDuration(160);
        sy.setDuration(160);
        sx.start();
        sy.start();
    }

    private void floatTextAt(String text, float x, float y, int color, float sp) {
        if (mOverlay == null || getView() == null) return;
        TextView t = new TextView(requireContext());
        t.setText(text);
        t.setTextColor(color);
        t.setTextSize(sp);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setShadowLayer(5f, 0, 1, Color.BLACK);
        t.setPadding(dp(4), 0, dp(4), 0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int) x;
        lp.topMargin = (int) y;
        t.setLayoutParams(lp);
        mOverlay.addView(t);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(t, "translationY", 0, -dp(70)),
                ObjectAnimator.ofFloat(t, "alpha", 1f, 0f)
        );
        set.setDuration(800);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mOverlay.removeView(t);
            }
        });
        set.start();
    }

    private void floatTextTop(String text, int color, float sp) {
        if (mOverlay == null || getView() == null) return;
        final View root = getView();
        root.post(() -> {
            if (!isAdded() || mOverlay == null) return;
            TextView t = new TextView(requireContext());
            t.setText(text);
            t.setTextColor(color);
            t.setTextSize(sp);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setShadowLayer(5f, 0, 1, Color.BLACK);
            t.setPadding(dp(8), 0, dp(8), 0);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.topMargin = dp(96);
            t.setLayoutParams(lp);
            mOverlay.addView(t);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(t, "translationY", 0, -dp(70)),
                    ObjectAnimator.ofFloat(t, "alpha", 1f, 0f)
            );
            set.setDuration(900);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOverlay.removeView(t);
                }
            });
            set.start();
        });
    }

    private void showBanner(String text, int duration) {
        if (mOverlay == null || getView() == null) return;
        if (mBanner == null) {
            TextView b = new TextView(requireContext());
            b.setTextColor(C_TEXT);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setTextSize(12);
            b.setGravity(Gravity.CENTER);
            b.setBackground(pill());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.topMargin = dp(48);
            lp.leftMargin = dp(16);
            lp.rightMargin = dp(16);
            b.setPadding(dp(14), dp(8), dp(14), dp(8));
            b.setLayoutParams(lp);
            mOverlay.addView(b);
            mBanner = b;
        }
        mBanner.setText(text);
        mBanner.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mBannerHide);
        mBannerHide = () -> {
            if (mBanner != null) mBanner.setVisibility(View.GONE);
        };
        mHandler.postDelayed(mBannerHide, duration);
    }

    private void hideBanner() {
        mHandler.removeCallbacks(mBannerHide);
        if (mBanner != null) mBanner.setVisibility(View.GONE);
    }

    private void updateComboUi() {
        if (mOverlay == null || getView() == null) return;
        if (mComboCount >= 5) {
            if (mComboChip == null) {
                LinearLayout ll = new LinearLayout(requireContext());
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setGravity(Gravity.CENTER);
                ll.setBackground(drawable(Color.parseColor("#E612141D"), C_GOLD, dp(12)));
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM | Gravity.END;
                lp.bottomMargin = dp(16);
                lp.marginEnd = dp(12);
                ll.setPadding(dp(14), dp(8), dp(14), dp(8));
                ll.setLayoutParams(lp);
                TextView t1 = new TextView(requireContext());
                t1.setTextColor(C_GOLD);
                t1.setTextSize(12);
                t1.setTypeface(Typeface.DEFAULT_BOLD);
                ll.addView(t1);
                TextView t2 = new TextView(requireContext());
                t2.setTextColor(C_TEXT);
                t2.setTextSize(10);
                ll.addView(t2);
                mOverlay.addView(ll);
                mComboChip = ll;
                mComboChipT1 = t1;
                mComboChipT2 = t2;
            }
            mComboChipT1.setText("⚡ " + mComboCount + "× Kombo-Serie!");
            mComboChipT2.setText("Multiplikator: " + String.format(Locale.GERMANY, "%.2f", comboMul()) + "×");
            mComboChip.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mComboHide);
            mComboHide = () -> {
                if (mComboChip != null) mComboChip.setVisibility(View.GONE);
            };
            mHandler.postDelayed(mComboHide, COMBO_WINDOW + 200);
        } else if (mComboChip != null) {
            mComboChip.setVisibility(View.GONE);
        }
    }

    private void hideCombo() {
        mHandler.removeCallbacks(mComboHide);
        if (mComboChip != null) mComboChip.setVisibility(View.GONE);
    }

    private void startEventStream() {
        if (mSseThread != null) return;
        mSseStop = false;
        mSseThread = new Thread(() -> {
            while (!mSseStop && mRunning) {
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL(KollegenApi.BASE_URL + EVENT_STREAM_URL).openConnection();
                    mSseConn = conn;
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(0);
                    String cookie = KollegenSession.cookie();
                    if (cookie != null && !cookie.isEmpty()) conn.setRequestProperty("Cookie", cookie);
                    conn.setRequestProperty("Accept", "text/event-stream");
                    BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line;
                    while (!mSseStop && mRunning && (line = r.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            final String data = line.substring(6);
                            try {
                                JSONObject obj = new JSONObject(data);
                                mHandler.post(() -> dispatchEvent(obj));
                            } catch (Exception ignored) {}
                        }
                    }
                    r.close();
                } catch (Exception ignored) {
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                        } catch (Exception ignored) {}
                    }
                    mSseConn = null;
                }
                if (mSseStop || !mRunning) break;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "kollegen-sse");
        mSseThread.start();
    }

    private void stopEventStream() {
        mSseStop = true;
        if (mSseConn != null) {
            try {
                mSseConn.disconnect();
            } catch (Exception ignored) {}
        }
        mSseConn = null;
        mSseThread = null;
    }

    private void dispatchEvent(JSONObject ev) {
        if (!isAdded() || getView() == null) return;
        String type = ev.optString("type");
        if ("game_event".equals(type)) {
            startEvent(ev.optString("eventType"), ev.optJSONObject("eventState"));
        } else if ("game_event_end".equals(type)) {
            endEvent();
        } else if ("happi_hp_update".equals(type)) {
            if (mBossOpen) {
                mBossHp = ev.optInt("happiHp", mBossHp);
                mBossMaxHp = ev.optInt("maxHp", Math.max(1, mBossMaxHp));
                updateBossHp();
            }
        } else if ("happi_defeated".equals(type)) {
            showBanner("🏆 " + ev.optString("slayerName", "Ein Kollege") + " hat den World-Boss Happi besiegt! (+1.000.000 Cookies) 🎉", 8000);
            closeBoss();
        }
    }

    private void startEvent(String eventType, JSONObject state) {
        if (eventType == null) return;
        if ("happi".equals(eventType)) {
            mBossMaxHp = state != null && state.has("maxHp") ? state.optInt("maxHp", 200) : 200;
            mBossHp = state != null && state.has("happiHp") ? state.optInt("happiHp", mBossMaxHp) : mBossMaxHp;
            openBoss();
            showBanner("🌍 GEMEINSAMER WORLD-BOSS: BESIEGT HAPPI ZUSAMMEN FÜR 1M COOKIES!", 6000);
        } else if ("invasion".equals(eventType)) {
            startInvasion();
            showBanner("🚀 KOLLEGEN INVASION! KLICKE DIE FLIEGENDEN KOLLEGEN FÜR 2X COOKIES!", 6000);
        } else if ("rage".equals(eventType)) {
            startRage();
            showBanner("🔥 SERVER-RAGE AKTIV! 10 SEKUNDEN LANG +50% MEHR KLICKS & CPS!", 6000);
        }
    }

    private void endEvent() {
        closeBoss();
        clearInvasion();
    }

    private void openBoss() {
        if (mOverlay == null || getView() == null) return;
        closeBoss();
        clearInvasion();
        mBossOpen = true;

        FrameLayout bg = new FrameLayout(requireContext());
        bg.setBackgroundColor(0xCC0A0B0E);
        bg.setClickable(true);
        bg.setFocusable(true);
        bg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout panel = new LinearLayout(requireContext());
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setBackground(drawable(C_PANEL, C_GOLD, dp(24)));
        panel.setPadding(dp(20), dp(20), dp(20), dp(20));
        FrameLayout.LayoutParams panLp = new FrameLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.WRAP_CONTENT);
        panLp.gravity = Gravity.CENTER;
        panel.setLayoutParams(panLp);

        TextView tag = new TextView(requireContext());
        tag.setText("🌍 GEMEINSAMER SERVER WORLD-BOSS");
        tag.setTextColor(C_GOLD);
        tag.setTextSize(11);
        tag.setTypeface(Typeface.DEFAULT_BOLD);
        tag.setGravity(Gravity.CENTER);
        panel.addView(tag);

        TextView title = new TextView(requireContext());
        title.setText("Besiegt Happi zusammen!");
        title.setTextColor(C_TEXT);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(6), 0, 0);
        panel.addView(title);

        TextView sub = new TextView(requireContext());
        sub.setText("Alle aktiven Spieler greifen denselben Boss an! Wer den letzten Schlag landet, gewinnt 1.000.000 Cookies.");
        sub.setTextColor(C_MUTED);
        sub.setTextSize(11);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(6), 0, 0);
        panel.addView(sub);

        mBossImage = new ImageView(requireContext());
        mBossImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        FrameLayout.LayoutParams imLp = new FrameLayout.LayoutParams(dp(150), dp(150));
        imLp.gravity = Gravity.CENTER_HORIZONTAL;
        imLp.topMargin = dp(12);
        mBossImage.setLayoutParams(imLp);
        mBossImage.setClickable(true);
        mBossImage.setOnClickListener(v -> happiHit());
        loadSkinUrl(mBossImage, KollegenApi.BASE_URL + HAPPI_IMAGE);
        panel.addView(mBossImage);

        TextView hpLabel = new TextView(requireContext());
        hpLabel.setGravity(Gravity.CENTER);
        hpLabel.setTextColor(C_TEXT);
        hpLabel.setTextSize(12);
        hpLabel.setTypeface(Typeface.DEFAULT_BOLD);
        hpLabel.setPadding(0, dp(10), 0, dp(4));
        mBossHpText = hpLabel;
        panel.addView(hpLabel);

        FrameLayout hpBg = new FrameLayout(requireContext());
        FrameLayout.LayoutParams hpBgLp = new FrameLayout.LayoutParams(dp(250), dp(16));
        hpBgLp.gravity = Gravity.CENTER_HORIZONTAL;
        hpBg.setLayoutParams(hpBgLp);
        hpBg.setBackground(drawable(C_PROGRESS, 0, dp(8)));
        hpBg.setPadding(dp(2), dp(2), dp(2), dp(2));
        View hpFill = new View(requireContext());
        hpFill.setBackground(goldBar());
        FrameLayout.LayoutParams fillLp = new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        hpFill.setLayoutParams(fillLp);
        hpBg.addView(hpFill);
        mBossHpFill = hpFill;
        panel.addView(hpBg);

        Button attack = new Button(requireContext());
        attack.setText("⚔️ ANGREIFEN ⚔️");
        attack.setTextSize(15);
        attack.setTypeface(Typeface.DEFAULT_BOLD);
        attack.setTextColor(C_BG);
        attack.setBackground(goldButton());
        attack.setMinWidth(0);
        FrameLayout.LayoutParams atLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        atLp.gravity = Gravity.CENTER_HORIZONTAL;
        atLp.topMargin = dp(14);
        attack.setLayoutParams(atLp);
        attack.setAllCaps(true);
        attack.setOnClickListener(v -> happiHit());
        panel.addView(attack);

        bg.addView(panel);
        mOverlay.addView(bg);
        mBossOverlay = bg;
        updateBossHp();
    }

    private void happiHit() {
        if (!mBossOpen || mBossHp <= 0) return;
        mBossHp = Math.max(0, mBossHp - 1);
        updateBossHp();
        floatTextAt("💥 -1", dp(10), dp(120), C_GOLD, 14);
        if (mBossImage != null) pressScale(mBossImage, 0.9f);
        long now = System.currentTimeMillis();
        if (now - mLastHappiHit < 120) return;
        mLastHappiHit = now;
        KollegenApi.post(HAPPI_HIT_URL, new JSONObject(), new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded() || !(json instanceof JSONObject)) return;
                JSONObject res = (JSONObject) json;
                if (!res.optBoolean("defeated")) return;
                closeBoss();
                if (res.optBoolean("isSlayer")) {
                    mClicks += 1000000;
                    mTotalClicks += 1000000;
                    updateStats();
                    showBanner("🎉 DU HAST HAPPI BESIEGT! +1.000.000 COOKIES! 🏆", 8000);
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void updateBossHp() {
        if (mBossHpFill == null || mBossHpText == null || getView() == null) return;
        mBossHpText.setText("HP " + mBossHp + " / " + mBossMaxHp);
        final View parent = (View) mBossHpFill.getParent();
        mBossHpFill.post(() -> {
            if (parent == null || mBossHpFill == null) return;
            float pct = mBossMaxHp <= 0 ? 0 : (float) mBossHp / mBossMaxHp;
            ViewGroup.LayoutParams lp = mBossHpFill.getLayoutParams();
            int target = (int) (parent.getWidth() * Math.max(0f, Math.min(1f, pct)));
            if (lp.width != target) {
                lp.width = target;
                mBossHpFill.setLayoutParams(lp);
            }
        });
    }

    private void closeBoss() {
        if (mBossOverlay != null && mOverlay != null) {
            mOverlay.removeView(mBossOverlay);
        }
        mBossOverlay = null;
        mBossImage = null;
        mBossHpFill = null;
        mBossHpText = null;
        mBossOpen = false;
    }

    private void startInvasion() {
        final View root = getView();
        if (root == null || mOverlay == null) return;
        final int w = root.getWidth();
        final int h = root.getHeight();
        if (w <= 0 || h <= 0) {
            mHandler.post(this::startInvasion);
            return;
        }
        clearInvasion();
        int count = 14;
        for (int i = 0; i < count; i++) {
            int size = (int) (dp(42) + Math.random() * dp(24));
            ImageView img = new ImageView(requireContext());
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            float fx = (float) (Math.random() * Math.max(1, w - size));
            float fy = (float) (dp(80) + Math.random() * Math.max(1, h - size - dp(160)));
            lp.leftMargin = (int) fx;
            lp.topMargin = (int) fy;
            img.setLayoutParams(lp);
            img.setAlpha(0.92f);
            loadSkinUrl(img, KollegenApi.BASE_URL + INVASION_IMAGE);
            final float vx = (float) ((Math.random() - 0.5) * 7);
            final float vy = (float) ((Math.random() - 0.5) * 7);
            final float[] vel = new float[]{fx, fy, vx, vy, size};
            mInvasionVels.add(vel);
            mInvasionViews.add(img);
            img.setOnClickListener(v -> popInvasion(img));
            mOverlay.addView(img);
        }
        mHandler.removeCallbacks(mInvasionTick);
        mHandler.removeCallbacks(mInvasionEnd);
        mHandler.post(mInvasionTick);
        mInvasionEnd = this::clearInvasion;
        mHandler.postDelayed(mInvasionEnd, 20000);
    }

    private void popInvasion(ImageView img) {
        if (!mInvasionViews.contains(img)) return;
        int idx = mInvasionViews.indexOf(img);
        mInvasionViews.remove(idx);
        if (idx < mInvasionVels.size()) mInvasionVels.remove(idx);
        if (mOverlay != null) mOverlay.removeView(img);
        int gain = Math.max(200, calcCpc() * 2);
        mClicks += gain;
        mTotalClicks += gain;
        updateStats();
        floatTextTop("💥 +" + mNf.format((long) gain) + " Cookies (2x Klick-Bonus)", C_GREEN, 16);
        if (mInvasionViews.isEmpty()) mHandler.removeCallbacks(mInvasionTick);
    }

    private void clearInvasion() {
        mHandler.removeCallbacks(mInvasionTick);
        mHandler.removeCallbacks(mInvasionEnd);
        mInvasionEnd = null;
        for (View v : mInvasionViews) {
            if (mOverlay != null) mOverlay.removeView(v);
        }
        mInvasionViews.clear();
        mInvasionVels.clear();
    }

    private void startRage() {
        if (mOverlay == null || getView() == null) return;
        mRageUntil = System.currentTimeMillis() + 10000;
        if (mRageChip == null) {
            TextView chip = new TextView(requireContext());
            chip.setTextColor(C_TEXT);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setTextSize(11);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(drawable(C_RAGE, Color.parseColor("#FBBF24"), dp(10)));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.topMargin = dp(10);
            lp.leftMargin = dp(10);
            lp.rightMargin = dp(10);
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            chip.setLayoutParams(lp);
            mOverlay.addView(chip);
            mRageChip = chip;
        }
        mHandler.removeCallbacks(mRageTick);
        mHandler.post(mRageTick);
    }

    private void removeRage() {
        mHandler.removeCallbacks(mRageTick);
        if (mRageChip != null && mOverlay != null) mOverlay.removeView(mRageChip);
        mRageChip = null;
    }

    private int calcCpc() {
        long base = 1;
        if (mUpgradeConfig != null) {
            for (int i = 0; i < mUpgradeConfig.length(); i++) {
                JSONObject u = mUpgradeConfig.optJSONObject(i);
                if (u == null) continue;
                int count = mUpgrades.containsKey(u.optString("id")) ? mUpgrades.get(u.optString("id")) : 0;
                base += (long) u.optInt("cpcBonus", 0) * count;
            }
        }
        double skinMul = skinData(mEquippedSkin, "cpcMultiplier");
        double comboMul = comboMul();
        return (int) Math.max(1, Math.round(base * skinMul * comboMul));
    }

    private int calcCps() {
        long base = 0;
        if (mUpgradeConfig != null) {
            for (int i = 0; i < mUpgradeConfig.length(); i++) {
                JSONObject u = mUpgradeConfig.optJSONObject(i);
                if (u == null) continue;
                int count = mUpgrades.containsKey(u.optString("id")) ? mUpgrades.get(u.optString("id")) : 0;
                base += (long) u.optInt("cpsBonus", 0) * count;
            }
        }
        double skinMul = skinData(mEquippedSkin, "cpsMultiplier");
        return (int) Math.round(base * skinMul);
    }

    private double skinData(String skinId, String field) {
        if (mSkinConfig == null || skinId == null) return 1;
        for (int i = 0; i < mSkinConfig.length(); i++) {
            JSONObject s = mSkinConfig.optJSONObject(i);
            if (s != null && skinId.equals(s.optString("id"))) {
                return s.optDouble(field, 1);
            }
        }
        return 1;
    }

    private String skinImage(String skinId) {
        if (mSkinConfig == null || skinId == null) return null;
        for (int i = 0; i < mSkinConfig.length(); i++) {
            JSONObject s = mSkinConfig.optJSONObject(i);
            if (s != null && skinId.equals(s.optString("id"))) {
                String img = s.optString("image", "");
                return img.isEmpty() ? null : img;
            }
        }
        return null;
    }

    private double comboMul() {
        if (mComboCount >= 200) return 3;
        if (mComboCount >= 100) return 2.5;
        if (mComboCount >= 50) return 2;
        if (mComboCount >= 25) return 1.5;
        if (mComboCount >= 10) return 1.25;
        return 1;
    }

    private int getUpgradeCost(JSONObject upgrade, int owned) {
        double base = upgrade.optDouble("baseCost", 1);
        return (int) Math.max(1, Math.floor(base * Math.pow(1.15, owned)));
    }

    private int getLevel(long totalClicks) {
        if (totalClicks < 2500) return 1;
        return (int) (Math.floor(Math.pow(totalClicks / 2500.0, 1.0 / 2.4)) + 1);
    }

    private double getLevelProgress(long totalClicks) {
        int level = getLevel(totalClicks);
        long base = levelBase(level);
        long next = levelBase(level + 1);
        long diff = next - base;
        if (diff <= 0) return 100;
        return Math.min(100, Math.max(0, (totalClicks - base) * 100.0 / diff));
    }

    private long levelBase(int level) {
        if (level <= 1) return 0;
        return (long) Math.floor(2500 * Math.pow(level - 1, 2.4));
    }

    private void updateStats() {
        if (getView() == null) return;
        int cpc = calcCpc();
        int cps = calcCps();
        int level = getLevel(mTotalClicks);
        double progress = getLevelProgress(mTotalClicks);
        mLevel.setText("\u2726 LEVEL " + level);
        mLevelSub.setText(String.format(Locale.GERMANY, "%.1f%% bis Lv. %d", progress, level + 1));
        mCount.setText(mNf.format(mTotalClicks));
        mBalance.setText(mNf.format(mClicks));
        mClickVal.setText("+" + mNf.format(cpc));
        mCps.setText("+" + mNf.format(cps));
        mSkinPill.setText(String.format(Locale.GERMANY, "Skin: %s \u00b7 \u00c4ndern \ud83c\udfa8", skinName(mEquippedSkin)));
        mProgressBar.post(() -> {
            int w = ((View) mProgressBar.getParent()).getWidth();
            int barW = (int) (w * progress / 100.0);
            ViewGroup.LayoutParams lp = mProgressBar.getLayoutParams();
            if (lp.width != barW) {
                lp.width = barW;
                mProgressBar.setLayoutParams(lp);
            }
        });
    }

    private String skinName(String skinId) {
        if (mSkinConfig != null) {
            for (int i = 0; i < mSkinConfig.length(); i++) {
                JSONObject s = mSkinConfig.optJSONObject(i);
                if (s != null && skinId.equals(s.optString("id"))) {
                    return s.optString("name", skinId);
                }
            }
        }
        return skinId;
    }

    private void checkAchievements() {
        if (mAchievementConfig == null) return;
        for (int i = 0; i < mAchievementConfig.length(); i++) {
            JSONObject a = mAchievementConfig.optJSONObject(i);
            if (a == null) continue;
            String id = a.optString("id", "");
            if (mUnlockedAchievements.contains(id)) continue;
            if (a.optInt("requiredClicks", 0) > 0 && mTotalClicks < a.optInt("requiredClicks", 0)) continue;
            mUnlockedAchievements.add(id);
            int reward = a.optInt("rewardClicks", 0);
            if (reward > 0) {
                mClicks += reward;
                mTotalClicks += reward;
            }
            floatTextTop("🏆 Erfolg: " + a.optString("name", id) + "!", C_GOLD, 16);
            showTab(mCurrentTab);
        }
    }

    private void claimAchievement(String id) {
        if (mClaimedAchievements.contains(id)) return;
        if (mAchievementConfig == null) return;
        for (int i = 0; i < mAchievementConfig.length(); i++) {
            JSONObject a = mAchievementConfig.optJSONObject(i);
            if (a != null && id.equals(a.optString("id"))) {
                mClaimedAchievements.add(id);
                int reward = a.optInt("rewardClicks", 0);
                if (reward > 0) {
                    mClicks += reward;
                    mTotalClicks += reward;
                }
                floatTextTop("+" + mNf.format((long) reward) + " Klicks Belohnung! 🎁", C_GREEN, 16);
                updateStats();
                showTab(1);
                break;
            }
        }
    }

    private void syncToServer() {
        if (mSyncing || !KollegenSession.isLoggedIn() || getView() == null) return;
        mSyncing = true;
        JSONObject body = new JSONObject();
        try {
            body.put("clicks", mClicks);
            body.put("totalClicks", mTotalClicks);
            JSONObject su = new JSONObject();
            for (Map.Entry<String, Integer> e : mUpgrades.entrySet()) su.put(e.getKey(), e.getValue());
            body.put("upgrades", su);
            JSONArray ua = new JSONArray();
            for (String s : mUnlockedAchievements) ua.put(s);
            body.put("unlockedAchievements", ua);
            JSONArray ca = new JSONArray();
            for (String s : mClaimedAchievements) ca.put(s);
            body.put("claimedAchievements", ca);
            JSONArray us = new JSONArray();
            for (String s : mUnlockedSkins) us.put(s);
            body.put("unlockedSkins", us);
            body.put("equippedSkin", mEquippedSkin);
            body.put("prestigeLevel", 0);
            body.put("prestigeStars", 0);
            body.put("prestigeUpgrades", new JSONObject());
        } catch (Exception e) {
            mSyncing = false;
            return;
        }
        KollegenApi.post(SYNC_URL, body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                mSyncing = false;
                if (!(json instanceof JSONObject)) return;
                JSONObject res = (JSONObject) json;
                JSONObject state = res.optJSONObject("state");
                if (state == null) return;
                long serverClicks = (long) state.optDouble("clicks", 0);
                if (serverClicks > mClicks) mClicks = serverClicks;
                long serverTotal = (long) state.optDouble("totalClicks", 0);
                if (serverTotal > mTotalClicks) mTotalClicks = serverTotal;
            }

            @Override
            public void onError(String message) {
                mSyncing = false;
            }
        });
    }

    private void showTab(int tab) {
        mCurrentTab = tab;
        String skinCount = mSkinConfig == null ? "" : (mUnlockedSkins.size() + "/" + mSkinConfig.length());
        String achCount = mAchievementConfig == null ? "" : (mUnlockedAchievements.size() + "/" + mAchievementConfig.length());
        mTabUpgrades.setText("Upgrades");
        mTabSkins.setText("Skins (" + skinCount + ")");
        mTabAchievements.setText("Erfolge (" + achCount + ")");
        mTabUpgrades.setTextColor(tab == 0 ? C_GOLD : C_TEXT);
        mTabSkins.setTextColor(tab == 2 ? C_GOLD : C_TEXT);
        mTabAchievements.setTextColor(tab == 1 ? C_GOLD : C_TEXT);
        mContent.removeAllViews();
        if (tab == 0) renderUpgrades();
        else if (tab == 1) renderAchievements();
        else renderSkins();
    }

    private void renderUpgrades() {
        if (mUpgradeConfig == null) {
            mContent.addView(empty("L\u00e4dt..."));
            return;
        }
        TextView ownedCount = new TextView(requireContext());
        ownedCount.setText("\ud83c\udfea Upgrades & Helfer (" + (mUpgrades.size()) + ")");
        ownedCount.setTextColor(C_TEXT);
        ownedCount.setTextSize(13);
        ownedCount.setTypeface(Typeface.DEFAULT_BOLD);
        mContent.addView(ownedCount);

        TextView guthaben = new TextView(requireContext());
        guthaben.setText("Guthaben: " + mNf.format(mClicks));
        guthaben.setTextColor(C_GOLD);
        guthaben.setTextSize(12);
        guthaben.setPadding(0, dp(2), 0, dp(8));
        mContent.addView(guthaben);

        for (int i = 0; i < mUpgradeConfig.length(); i++) {
            JSONObject u = mUpgradeConfig.optJSONObject(i);
            if (u == null) continue;
            String id = u.optString("id");
            String name = u.optString("name");
            String icon = u.optString("icon");
            String desc = u.optString("description");
            int cpcB = u.optInt("cpcBonus", 0);
            int cpsB = u.optInt("cpsBonus", 0);
            int owned = mUpgrades.containsKey(id) ? mUpgrades.get(id) : 0;
            int cost = getUpgradeCost(u, owned);
            boolean canBuy = mClicks >= cost;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int[] rowBg = new int[]{C_PANEL, canBuy ? C_GOLD : C_STONE};
            row.setBackground(xpand(rowBg));

            LinearLayout iconBox = new LinearLayout(requireContext());
            iconBox.setGravity(Gravity.CENTER);
            iconBox.setPadding(dp(2), 0, dp(4), 0);
            TextView iconTv = new TextView(requireContext());
            iconTv.setText(icon);
            iconTv.setTextSize(20);
            iconBox.addView(iconTv);
            row.addView(iconBox);

            LinearLayout info = new LinearLayout(requireContext());
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            infoLp.setMargins(0, 0, dp(8), 0);
            info.setLayoutParams(infoLp);

            TextView nameTv = new TextView(requireContext());
            nameTv.setText(name);
            nameTv.setTextColor(C_TEXT);
            nameTv.setTextSize(13);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            info.addView(nameTv);

            TextView descTv = new TextView(requireContext());
            descTv.setText(desc);
            descTv.setTextColor(C_MUTED);
            descTv.setTextSize(11);
            info.addView(descTv);

            StringBuilder bonus = new StringBuilder();
            if (cpsB > 0) bonus.append("+").append(cpsB).append(" CPS");
            if (cpcB > 0) { if (bonus.length() > 0) bonus.append("  "); bonus.append("+").append(cpcB).append(" CPC"); }
            if (owned > 0) { if (bonus.length() > 0) bonus.append("  "); bonus.append("Lv. ").append(owned); }
            TextView bonusTv = new TextView(requireContext());
            bonusTv.setText(bonus.toString());
            bonusTv.setTextColor(C_GOLD);
            bonusTv.setTextSize(11);
            info.addView(bonusTv);

            row.addView(info);

            Button buyBtn = new Button(requireContext());
            buyBtn.setText(mNf.format(cost) + " Klicks");
            buyBtn.setTextSize(11);
            buyBtn.setTypeface(Typeface.DEFAULT_BOLD);
            buyBtn.setTextColor(canBuy ? C_BG : C_MUTED);
            buyBtn.setBackground(canBuy ? goldButton() : stoneButton());
            buyBtn.setPadding(dp(10), 0, dp(10), 0);
            buyBtn.setMinWidth(0);
            buyBtn.setEnabled(canBuy);
            final JSONObject fU = u;
            final int fCost = cost;
            if (canBuy) {
                buyBtn.setOnClickListener(v -> buyUpgrade(fU, fCost));
            }
            row.addView(buyBtn);
            mContent.addView(row);
        }
    }

    private void buyUpgrade(JSONObject u, int cost) {
        if (mClicks < cost) return;
        mClicks -= cost;
        String id = u.optString("id");
        mUpgrades.put(id, (mUpgrades.containsKey(id) ? mUpgrades.get(id) : 0) + 1);
        updateStats();
        showTab(0);
    }

    private void renderAchievements() {
        if (mAchievementConfig == null) {
            mContent.addView(empty("L\u00e4dt..."));
            return;
        }
        TextView head = new TextView(requireContext());
        head.setText("\ud83c\udfc5 Erfolge");
        head.setTextColor(C_TEXT);
        head.setTextSize(13);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        mContent.addView(head);

        for (int i = 0; i < mAchievementConfig.length(); i++) {
            JSONObject a = mAchievementConfig.optJSONObject(i);
            if (a == null) continue;
            String id = a.optString("id");
            String name = a.optString("name");
            String icon = a.optString("icon");
            int req = a.optInt("requiredClicks", 0);
            int reward = a.optInt("rewardClicks", 0);
            boolean unlocked = mUnlockedAchievements.contains(id);
            boolean claimed = mClaimedAchievements.contains(id);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(drawable(C_PANEL, C_STONE));

            TextView iconTv = new TextView(requireContext());
            iconTv.setText(icon);
            iconTv.setTextSize(22);
            if (!unlocked) iconTv.setAlpha(0.3f);
            iconTv.setPadding(0, 0, dp(6), 0);
            row.addView(iconTv);

            LinearLayout info = new LinearLayout(requireContext());
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            info.setLayoutParams(infoLp);

            TextView nameTv = new TextView(requireContext());
            nameTv.setText(name);
            nameTv.setTextColor(unlocked ? C_TEXT : C_MUTED);
            nameTv.setTextSize(13);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            info.addView(nameTv);

            TextView descTv = new TextView(requireContext());
            descTv.setText(req > 0 ? mNf.format((long) req) + " Klicks n\u00f6tig" : a.optString("description", ""));
            descTv.setTextColor(C_MUTED);
            descTv.setTextSize(11);
            info.addView(descTv);

            row.addView(info);

            if (unlocked && !claimed) {
                Button claimBtn = tinyButton("+" + mNf.format((long) reward));
                claimBtn.setOnClickListener(v -> claimAchievement(id));
                row.addView(claimBtn);
            } else if (claimed) {
                TextView cv = new TextView(requireContext());
                cv.setText("\u2713");
                cv.setTextColor(C_GREEN);
                cv.setTextSize(16);
                cv.setPadding(dp(8), 0, 0, 0);
                row.addView(cv);
            }
            mContent.addView(row);
        }
    }

    private void renderSkins() {
        if (mSkinConfig == null) {
            mContent.addView(empty("L\u00e4dt..."));
            return;
        }
        TextView head = new TextView(requireContext());
        head.setText("\ud83c\udfa8 Skins");
        head.setTextColor(C_TEXT);
        head.setTextSize(13);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        mContent.addView(head);

        LinearLayout grid = new LinearLayout(requireContext());
        grid.setOrientation(LinearLayout.HORIZONTAL);
        mContent.addView(grid);
        int perRow = 3;
        for (int i = 0; i < mSkinConfig.length(); i++) {
            JSONObject s = mSkinConfig.optJSONObject(i);
            if (s == null) continue;
            String id = s.optString("id");
            String name = s.optString("name");
            String image = s.optString("image", "");
            int cost = s.optInt("cost", 0);
            boolean owned = mUnlockedSkins.contains(id);
            boolean equipped = id.equals(mEquippedSkin);

            if (i > 0 && i % perRow == 0) {
                grid = new LinearLayout(requireContext());
                grid.setOrientation(LinearLayout.HORIZONTAL);
                mContent.addView(grid);
            }

            FrameLayout tile = new FrameLayout(requireContext());
            int sz = dp(100);
            LinearLayout.LayoutParams tileLp = new LinearLayout.LayoutParams(sz, sz);
            tileLp.setMargins(0, 0, dp(7), dp(7));
            tile.setLayoutParams(tileLp);
            tile.setBackground(drawable(C_PANEL, equipped ? C_GOLD : C_STONE));

            FrameLayout imgHolder = new FrameLayout(requireContext());
            FrameLayout.LayoutParams ihLp = new FrameLayout.LayoutParams(sz - dp(20), sz - dp(34));
            ihLp.gravity = Gravity.CENTER | Gravity.TOP;
            ihLp.topMargin = dp(6);
            imgHolder.setLayoutParams(ihLp);
            ImageView img = new ImageView(requireContext());
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imgHolder.addView(img);
            if (image.startsWith("/")) image = KollegenApi.BASE_URL + image;
            loadSkinUrl(img, image);
            tile.addView(imgHolder);

            TextView nm = new TextView(requireContext());
            FrameLayout.LayoutParams nmLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nmLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            nmLp.bottomMargin = dp(6);
            nm.setLayoutParams(nmLp);
            nm.setText(name);
            nm.setTextColor(equipped ? C_GOLD : C_TEXT);
            nm.setTextSize(11);
            nm.setTypeface(Typeface.DEFAULT_BOLD);
            tile.addView(nm);

            TextView state = new TextView(requireContext());
            FrameLayout.LayoutParams stLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            stLp.topMargin = dp(4);
            state.setLayoutParams(stLp);
            state.setText(equipped ? "Ausger\u00fcstet" : owned ? "Gekauft" : mNf.format((long) cost) + " Klicks");
            state.setTextColor(equipped ? C_GOLD : C_MUTED);
            state.setTextSize(9);
            tile.addView(state);

            final String fId = id;
            final int fCost = cost;
            final boolean fOwned = owned;
            tile.setOnClickListener(v -> {
                if (fOwned) {
                    mEquippedSkin = fId;
                    updateStats();
                    reloadSkinImage();
                    showTab(2);
                } else if (mClicks >= fCost) {
                    mClicks -= fCost;
                    mUnlockedSkins.add(fId);
                    mEquippedSkin = fId;
                    updateStats();
                    reloadSkinImage();
                    showTab(2);
                }
            });
            grid.addView(tile);
        }
    }

    private void reloadSkinImage() {
        if (getView() == null || mClickButton == null) return;
        String image = skinImage(mEquippedSkin);
        if (image == null || image.isEmpty()) return;
        if (image.startsWith("/")) image = KollegenApi.BASE_URL + image;
        final String url = image;
        mHandler.post(() -> {
            if (getView() == null) return;
            loadSkinUrl(mClickButton, url);
        });
    }

    private void loadSkinUrl(final ImageView target, final String url) {
        new Thread(() -> {
            byte[] data = fetchBytes(url);
            if (data == null) return;
            mHandler.post(() -> {
                if (!isAdded() || getView() == null) return;
                try {
                    if (url.toLowerCase(Locale.ROOT).endsWith(".gif")) {
                        GifDrawable gif = new GifDrawable(data);
                        target.setImageDrawable(gif);
                        gif.start();
                    } else {
                        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bm != null) target.setImageBitmap(bm);
                    }
                } catch (Exception ignored) {}
            });
        }).start();
    }

    private byte[] fetchBytes(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "KollegenAndroid/0.9");
            if (conn.getResponseCode() != 200) return null;
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            in.close();
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void applyTheme(View view) {
        view.setBackgroundColor(C_BG);
        view.findViewById(R.id.koll_clicker_level_panel).setBackground(borderGold());
        view.findViewById(R.id.koll_clicker_progress_bar_bg).setBackground(mkProgressBg());
        mProgressBar.setBackground(goldBar());
        mLevel.setTextColor(C_GOLD);
        mLevelSub.setTextColor(C_MUTED);
        mCount.setTextColor(C_TEXT);
        ((TextView) view.findViewById(R.id.koll_clicker_count_label)).setTextColor(C_MUTED);
        styleStatBox(view, R.id.koll_clicker_stat_balance_box, C_TEXT);
        styleStatBox(view, R.id.koll_clicker_stat_clickval_box, C_GOLD);
        styleStatBox(view, R.id.koll_clicker_stat_cps_box, C_GOLD);
        view.findViewById(R.id.koll_clicker_button_holder).setBackground(drawable(C_PANEL, C_GOLD, dp(24)));
        mSkinPill.setBackground(pill());
        mSkinPill.setTextColor(C_GOLD);
        mSkinPill.setTypeface(Typeface.DEFAULT_BOLD);
        mClickButton.setClipToOutline(true);
        TextView tip = view.findViewById(R.id.koll_clicker_tip);
        if (tip != null) {
            tip.setTextColor(C_MUTED);
            tip.setText("Tipp: Schnelle Klicks & Upgrades farmen Klicks auch, w\u00e4hrend der Tab aktiv ist!");
        }
        styleTab(mTabUpgrades);
        styleTab(mTabSkins);
        styleTab(mTabAchievements);
    }

    private void styleStatBox(View view, int boxId, int valueColor) {
        View box = view.findViewById(boxId);
        if (box != null) box.setBackground(drawable(C_PANEL, C_STONE, dp(8)));
        int n = box instanceof LinearLayout ? ((LinearLayout) box).getChildCount() : 0;
        for (int i = 0; i < n; i++) {
            View child = ((LinearLayout) box).getChildAt(i);
            if (child instanceof TextView) ((TextView) child).setTextColor(i == 0 ? valueColor : C_MUTED);
        }
    }

    private void styleTab(Button b) {
        b.setBackground(stoneButton());
        b.setTextColor(C_TEXT);
        b.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private GradientDrawable xpand(int[] colors) {
        return drawable(colors[0], colors[1], dp(10));
    }

    private GradientDrawable borderGold() {
        return drawable(C_GOLD_DARK, C_GOLD, dp(12));
    }

    private GradientDrawable mkProgressBg() {
        return drawable(C_PROGRESS, 0, dp(999));
    }

    private GradientDrawable goldBar() {
        return drawable(C_GOLD, 0, dp(999));
    }

    private GradientDrawable pill() {
        return drawable(Color.parseColor("#80000000"), 0, dp(999));
    }

    private GradientDrawable stoneButton() {
        return drawable(C_STONE, 0, dp(8));
    }

    private GradientDrawable goldButton() {
        return drawable(C_GOLD, 0, dp(8));
    }

    private Button tinyButton(String text) {
        Button b = new Button(requireContext());
        b.setText(text);
        b.setTextSize(11);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(C_BG);
        b.setBackground(goldButton());
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setMinWidth(0);
        return b;
    }

    private TextView empty(String text) {
        TextView t = new TextView(requireContext());
        t.setText(text);
        t.setTextColor(C_MUTED);
        t.setTextSize(12);
        t.setPadding(dp(4), dp(10), dp(4), dp(10));
        return t;
    }

    private GradientDrawable drawable(int fill, int stroke) {
        return drawable(fill, stroke, dp(10));
    }

    private GradientDrawable drawable(int fill, int stroke, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setColor(fill);
        if (stroke != 0) g.setStroke(dp(1), stroke);
        g.setCornerRadius(radius);
        return g;
    }

    private int dp(int v) {
        return KollegenKit.dp(requireContext(), v);
    }
}