package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class KollegenClickerFragment extends Fragment {
    private static final String SYNC_URL = "/api/clicker/sync";
    private static final String STATE_URL = "/api/clicker/state";
    private static final String CONFIG_URL = "/api/clicker/config";
    private static final long SYNC_INTERVAL = 15000;
    private static final long CPS_TICK = 1000;

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

    private JSONArray mUpgradeConfig;
    private JSONArray mAchievementConfig;
    private JSONArray mSkinConfig;

    private TextView mStats;
    private TextView mLevel;
    private View mProgressBg;
    private View mProgressBar;
    private Button mClickButton;
    private LinearLayout mContent;

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
        mStats = view.findViewById(R.id.koll_clicker_stats);
        mLevel = view.findViewById(R.id.koll_clicker_level);
        mProgressBg = view.findViewById(R.id.koll_clicker_progress_bar_bg);
        mProgressBar = view.findViewById(R.id.koll_clicker_progress_bar);
        mClickButton = view.findViewById(R.id.koll_clicker_button);
        mContent = view.findViewById(R.id.koll_clicker_content);
        Button tabUpgrades = view.findViewById(R.id.koll_clicker_tab_upgrades);
        Button tabAchievements = view.findViewById(R.id.koll_clicker_tab_achievements);
        Button tabSkins = view.findViewById(R.id.koll_clicker_tab_skins);

        mClickButton.setOnClickListener(v -> handleClick());
        tabUpgrades.setOnClickListener(v -> showTab(0));
        tabAchievements.setOnClickListener(v -> showTab(1));
        tabSkins.setOnClickListener(v -> showTab(2));
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
                unlockDefaultSkin();
                loadState();
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void unlockDefaultSkin() {
        mUnlockedSkins.add("default");
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
        String stats = mClicks + " Klicks\n" +
                "CPC: " + cpc + "  CPS: " + cps;
        mStats.setText(stats);
        mLevel.setText("Level " + level);
        mProgressBg.post(() -> {
            int w = mProgressBg.getWidth();
            int barW = (int) (w * progress / 100.0);
            ViewGroup.LayoutParams lp = mProgressBar.getLayoutParams();
            if (lp.width != barW) {
                lp.width = barW;
                mProgressBar.setLayoutParams(lp);
            }
        });
    }

    private void checkAchievements() {
        if (mAchievementConfig == null) return;
        for (int i = 0; i < mAchievementConfig.length(); i++) {
            JSONObject a = mAchievementConfig.optJSONObject(i);
            if (a == null) continue;
            String id = a.optString("id", "");
            if (mUnlockedAchievements.contains(id)) continue;
            boolean unlocked = false;
            int reqClicks = a.optInt("requiredClicks", 0);
            if (reqClicks > 0 && mTotalClicks >= reqClicks) unlocked = true;
            if (!unlocked) continue;
            mUnlockedAchievements.add(id);
            int reward = a.optInt("rewardClicks", 0);
            if (reward > 0) {
                mClicks += reward;
                mTotalClicks += reward;
            }
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
        if (mSyncing || !KollegenSession.isLoggedIn()) return;
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

    private int mCurrentTab = 0;

    private void showTab(int tab) {
        mCurrentTab = tab;
        mContent.removeAllViews();
        int[] pal = KollegenTheme.palette();
        Button b0 = getView() == null ? null : getView().findViewById(R.id.koll_clicker_tab_upgrades);
        Button b1 = getView() == null ? null : getView().findViewById(R.id.koll_clicker_tab_achievements);
        Button b2 = getView() == null ? null : getView() == null ? null : getView().findViewById(R.id.koll_clicker_tab_skins);
        if (b0 != null) b0.setTextColor(pal[tab == 0 ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
        if (b1 != null) b1.setTextColor(pal[tab == 1 ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
        if (b2 != null) b2.setTextColor(pal[tab == 2 ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
        if (tab == 0) renderUpgrades();
        else if (tab == 1) renderAchievements();
        else renderSkins();
    }

    private void renderUpgrades() {
        int[] pal = KollegenTheme.palette();
        if (mUpgradeConfig == null) {
            mContent.addView(KollegenKit.empty(requireContext(), "Lädt..."));
            return;
        }
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
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL2], canBuy ? pal[KollegenTheme.ACCENT] : pal[KollegenTheme.BORDER]));
            row.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, KollegenKit.dp(requireContext(), 6));
            row.setLayoutParams(lp);

            LinearLayout top = new LinearLayout(requireContext());
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView iconTv = new TextView(requireContext());
            iconTv.setText(icon);
            iconTv.setTextSize(20);
            top.addView(iconTv);

            LinearLayout info = new LinearLayout(requireContext());
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            infoLp.setMargins(KollegenKit.dp(requireContext(), 8), 0, 0, 0);
            info.setLayoutParams(infoLp);

            TextView nameTv = new TextView(requireContext());
            nameTv.setText(name);
            nameTv.setTextColor(pal[KollegenTheme.TEXT]);
            nameTv.setTextSize(14);
            nameTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            info.addView(nameTv);

            TextView descTv = new TextView(requireContext());
            descTv.setText(desc);
            descTv.setTextColor(pal[KollegenTheme.MUTED]);
            descTv.setTextSize(11);
            info.addView(descTv);

            top.addView(info);
            row.addView(top);

            LinearLayout bottom = new LinearLayout(requireContext());
            bottom.setOrientation(LinearLayout.HORIZONTAL);
            bottom.setGravity(Gravity.CENTER_VERTICAL);
            bottom.setPadding(0, KollegenKit.dp(requireContext(), 4), 0, 0);

            TextView bonusTv = new TextView(requireContext());
            StringBuilder bonus = new StringBuilder();
            if (cpsB > 0) bonus.append("+").append(cpsB).append(" CPS");
            if (cpcB > 0) { if (bonus.length() > 0) bonus.append("  "); bonus.append("+").append(cpcB).append(" CPC"); }
            bonusTv.setText(bonus.toString() + "  |  Owned: " + owned);
            bonusTv.setTextColor(pal[KollegenTheme.MUTED]);
            bonusTv.setTextSize(11);
            LinearLayout.LayoutParams bonusLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            bonusTv.setLayoutParams(bonusLp);
            bottom.addView(bonusTv);

            Button buyBtn = new Button(requireContext());
            buyBtn.setText(cost + " Klicks");
            buyBtn.setTextSize(11);
            buyBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            buyBtn.setTextColor(canBuy ? pal[KollegenTheme.BG] : pal[KollegenTheme.MUTED]);
            buyBtn.setBackground(canBuy ? KollegenTheme.buttonBackground(KollegenTheme.ACCENT, KollegenTheme.ACCENT2) : KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
            buyBtn.setPadding(KollegenKit.dp(requireContext(), 10), 0, KollegenKit.dp(requireContext(), 10), 0);
            buyBtn.setEnabled(canBuy);
            final JSONObject fU = u;
            final int fCost = cost;
            if (canBuy) {
                buyBtn.setOnClickListener(v -> buyUpgrade(fU, fCost));
            }
            bottom.addView(buyBtn);
            row.addView(bottom);
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
        int[] pal = KollegenTheme.palette();
        if (mAchievementConfig == null) {
            mContent.addView(KollegenKit.empty(requireContext(), "Lädt..."));
            return;
        }
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
            row.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL2], pal[KollegenTheme.BORDER]));
            row.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, KollegenKit.dp(requireContext(), 4));
            row.setLayoutParams(lp);

            TextView iconTv = new TextView(requireContext());
            iconTv.setText(icon);
            iconTv.setTextSize(22);
            if (!unlocked) iconTv.setAlpha(0.3f);
            row.addView(iconTv);

            LinearLayout info = new LinearLayout(requireContext());
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            infoLp.setMargins(KollegenKit.dp(requireContext(), 8), 0, 0, 0);
            info.setLayoutParams(infoLp);

            TextView nameTv = new TextView(requireContext());
            nameTv.setText(name);
            nameTv.setTextColor(pal[unlocked ? KollegenTheme.TEXT : KollegenTheme.MUTED]);
            nameTv.setTextSize(13);
            nameTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            info.addView(nameTv);

            TextView descTv = new TextView(requireContext());
            if (req > 0) descTv.setText(req + " Klicks nötig");
            descTv.setTextColor(pal[KollegenTheme.MUTED]);
            descTv.setTextSize(11);
            info.addView(descTv);

            row.addView(info);

            if (unlocked && !claimed) {
                Button claimBtn = KollegenKit.tinyButton(requireContext(), "+" + reward);
                claimBtn.setOnClickListener(v -> claimAchievement(id));
                row.addView(claimBtn);
            } else if (claimed) {
                TextView cv = new TextView(requireContext());
                cv.setText("✓");
                cv.setTextColor(pal[KollegenTheme.ACCENT]);
                cv.setTextSize(16);
                row.addView(cv);
            }
            mContent.addView(row);
        }
    }

    private void renderSkins() {
        int[] pal = KollegenTheme.palette();
        if (mSkinConfig == null) {
            mContent.addView(KollegenKit.empty(requireContext(), "Lädt..."));
            return;
        }
        LinearLayout grid = new LinearLayout(requireContext());
        grid.setOrientation(LinearLayout.HORIZONTAL);
        grid.setPadding(0, 0, 0, KollegenKit.dp(requireContext(), 8));
        mContent.addView(grid);
        int perRow = 3;
        for (int i = 0; i < mSkinConfig.length(); i++) {
            JSONObject s = mSkinConfig.optJSONObject(i);
            if (s == null) continue;
            String id = s.optString("id");
            String name = s.optString("name");
            String icon = s.optString("icon", "🎨");
            int cost = s.optInt("cost", 0);
            boolean owned = mUnlockedSkins.contains(id);
            boolean equipped = id.equals(mEquippedSkin);

            if (i > 0 && i % perRow == 0) {
                grid = new LinearLayout(requireContext());
                grid.setOrientation(LinearLayout.HORIZONTAL);
                mContent.addView(grid);
            }

            FrameLayout tile = new FrameLayout(requireContext());
            int sz = KollegenKit.dp(requireContext(), 80);
            LinearLayout.LayoutParams tileLp = new LinearLayout.LayoutParams(sz, sz);
            tileLp.setMargins(0, 0, KollegenKit.dp(requireContext(), 6), KollegenKit.dp(requireContext(), 6));
            tile.setLayoutParams(tileLp);
            tile.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL2], equipped ? pal[KollegenTheme.ACCENT] : pal[KollegenTheme.BORDER]));

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setGravity(Gravity.CENTER);
            inner.setPadding(KollegenKit.dp(requireContext(), 4), KollegenKit.dp(requireContext(), 4), KollegenKit.dp(requireContext(), 4), KollegenKit.dp(requireContext(), 4));

            TextView ic = new TextView(requireContext());
            ic.setText(icon);
            ic.setTextSize(24);
            ic.setGravity(Gravity.CENTER);
            inner.addView(ic);

            TextView nm = new TextView(requireContext());
            nm.setText(name);
            nm.setTextColor(pal[KollegenTheme.TEXT]);
            nm.setTextSize(10);
            nm.setGravity(Gravity.CENTER);
            inner.addView(nm);

            if (owned) {
                TextView eq = new TextView(requireContext());
                eq.setText(equipped ? "Ausgerüstet" : "Gekauft");
                eq.setTextColor(pal[equipped ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
                eq.setTextSize(9);
                eq.setGravity(Gravity.CENTER);
                inner.addView(eq);
            } else {
                TextView pr = new TextView(requireContext());
                pr.setText(cost + " Klicks");
                pr.setTextColor(pal[KollegenTheme.MUTED]);
                pr.setTextSize(9);
                pr.setGravity(Gravity.CENTER);
                inner.addView(pr);
            }

            tile.addView(inner);
            final String fId = id;
            final int fCost = cost;
            final boolean fOwned = owned;
            tile.setOnClickListener(v -> {
                if (fOwned) {
                    mEquippedSkin = fId;
                    showTab(2);
                } else if (mClicks >= fCost) {
                    mClicks -= fCost;
                    mUnlockedSkins.add(fId);
                    mEquippedSkin = fId;
                    updateStats();
                    showTab(2);
                }
            });
            grid.addView(tile);
        }
    }

    private void applyTheme(View view) {
        int[] pal = KollegenTheme.palette();
        view.setBackgroundColor(pal[KollegenTheme.BG]);
        mClickButton.setBackground(KollegenTheme.buttonBackground(KollegenTheme.ACCENT, KollegenTheme.ACCENT2));
        mClickButton.setTextColor(pal[KollegenTheme.BG]);
        mStats.setTextColor(pal[KollegenTheme.MUTED]);
        mLevel.setTextColor(pal[KollegenTheme.ACCENT]);
        mProgressBg.setBackgroundColor(pal[KollegenTheme.PANEL2]);
        mProgressBar.setBackgroundColor(pal[KollegenTheme.ACCENT]);
    }
}
