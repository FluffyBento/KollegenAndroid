package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

public class KollegenRootFragment extends Fragment {
    public static final String TAG = "kollegen_root";

    private Button mGamesTab;
    private Button mSocialTab;
    private Button mSettingsTab;
    private View mNavBar;
    private int mSelectedTab = -1;

    public KollegenRootFragment() {
        super(R.layout.fragment_kollegen_root);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mGamesTab = view.findViewById(R.id.koll_nav_games);
        mSocialTab = view.findViewById(R.id.koll_nav_social);
        mSettingsTab = view.findViewById(R.id.koll_nav_settings);
        mNavBar = view.findViewById(R.id.koll_nav_bar);

        mGamesTab.setOnClickListener(v -> showTab(0));
        mSocialTab.setOnClickListener(v -> showTab(1));
        mSettingsTab.setOnClickListener(v -> showTab(2));

        applyTheme(view);
        if (mSelectedTab < 0) showTab(0);
    }

    private void showTab(int index) {
        mSelectedTab = index;
        getChildFragmentManager().beginTransaction()
                .replace(R.id.koll_root_content, tabFragmentFor(index), "root_tab_" + index)
                .commit();
        updateTabColors();
    }

    private Fragment tabFragmentFor(int index) {
        switch (index) {
            case 1: return new KollegenTabHostFragment();
            case 2: return new KollegenSettingsFragment();
            default: return new KollegenGamesFragment();
        }
    }

    private void updateTabColors() {
        Button[] tabs = new Button[]{mGamesTab, mSocialTab, mSettingsTab};
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] == null) continue;
            tabs[i].setTextColor(KollegenTheme.color(i == mSelectedTab ? KollegenTheme.ACCENT : KollegenTheme.MUTED));
        }
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        if (mNavBar != null) mNavBar.setBackgroundColor(KollegenTheme.color(KollegenTheme.PANEL2));
        View title = view.findViewById(R.id.koll_root_title);
        if (title != null) ((android.widget.TextView) title).setTextColor(KollegenTheme.color(KollegenTheme.ACCENT));
        View tagline = view.findViewById(R.id.koll_root_tagline);
        if (tagline != null) ((android.widget.TextView) tagline).setTextColor(KollegenTheme.color(KollegenTheme.MUTED));
        View icon = view.findViewById(R.id.koll_root_icon);
        if (icon instanceof android.widget.ImageView) {
            ((android.widget.ImageView) icon).setColorFilter(KollegenTheme.color(KollegenTheme.ACCENT));
        }
        updateTabColors();
    }
}