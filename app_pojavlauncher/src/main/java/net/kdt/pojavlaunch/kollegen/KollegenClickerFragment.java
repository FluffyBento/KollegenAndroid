package net.kdt.pojavlaunch.kollegen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class KollegenClickerFragment extends Fragment {
    private static final String SYNC_URL = "/api/clicker/sync";
    private static final String STATE_URL = "/api/clicker/state";
    private static final String CONFIG_URL = "/api/clicker/config";
    private static final long SYNC_INTERVAL = 15000;
    private static final long CPS_TICK = 1000;

    private static final int C_BG = Color.parseColor("#08090C");
    private static final int C_PANEL = Color.parseColor("#12141D");
    private static final int C_STONE = Color.parseColor("#1C1F2B");
    private static final int C_GOLD = Color.parseColor("#FFAA00");
    private static final int C_GOLD_DARK = Color.parseColor("#241708");
    private static final int C_GREEN = Color.parseColor("#55FF55");
    private static final int C_TEXT = Color.parseColor("#FFFFFF");
    private static final int C_MUTED = Color.parseColor("#71717A");
    private static final int C_PROGRESS = Color.parseColor("#1A1D2A");

    private long mClicks;
    private long mTotalClicks;
    private final Map<String, Integer> mUpgrades = new HashMap<>();
    private final Set<String> mUnlockedAchievements = new HashSet<>();
    private final Set<String> mClaimedAchievements = new HashSet<>();
    private final Set<String> mUnlockedSkins = new HashSet<>();
    private String mEquippedSkin = "default";
    private int mComboCount;
    private long mLastClickTime;
    private boolean mSyncing;
    private boolean mRunning;
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

        mClickButton.setOnClickListener(v -> handleClick());
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
        mHandler.postDelayed(mCpsTick, CPS_TICK);
        mHandler.postDelayed(mSyncRunnable, SYNC_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunning = false;
        mHandler.removeCallbacks(mCpsTick);
        mHandler.removeCallbacks(mSyncRunnable);
        syncToServer();
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

    private void handleClick() {
        long now = System.currentTimeMillis();
        if (now - mLastClickTime > 1500) mComboCount = 0;
        mLastClickTime = now;
        mComboCount++;
        int cpc = calcCpc();
        mClicks += cpc;
        mTotalClicks += cpc;
        updateStats();
        checkAchievements();
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
            buyBtn.setInsetTop(0);
            buyBtn.setInsetBottom(0);
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
            loadSkinImage(img, image);
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
        ImageView target = mClickButton;
        String image = skinImage(mEquippedSkin);
        if (image == null || image.isEmpty()) return;
        if (image.startsWith("/")) image = KollegenApi.BASE_URL + image;
        final String url = image;
        mHandler.post(() -> {
            if (getView() == null) return;
            new Thread(() -> {
                Bitmap bm = fetchBitmap(url);
                if (bm == null) return;
                mHandler.post(() -> {
                    if (isAdded() && getView() != null) {
                        target.setImageBitmap(bm);
                    }
                });
            }).start();
        });
    }

    private void loadSkinImage(final ImageView img, final String url) {
        new Thread(() -> {
            Bitmap bm = fetchBitmap(url);
            if (bm == null) return;
            mHandler.post(() -> {
                if (isAdded()) img.setImageBitmap(bm);
            });
        }).start();
    }

    private Bitmap fetchBitmap(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "KollegenAndroid/0.9");
            if (conn.getResponseCode() != 200) return null;
            InputStream in = conn.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(in);
            in.close();
            return bm;
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
        b.setInsetTop(0);
        b.setInsetBottom(0);
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