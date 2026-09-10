package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

public class KollegenTabHostFragment extends Fragment {
    public static final String TAG = "kollegen_tabs";

    private Button mFriendsTab;
    private Button mProfileTab;
    private Button mShopTab;
    private int mSelectedTab = 0;

    public KollegenTabHostFragment() {
        super(R.layout.fragment_kollegen_tabs);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mFriendsTab = view.findViewById(R.id.koll_tab_friends);
        mProfileTab = view.findViewById(R.id.koll_tab_profile);
        mShopTab = view.findViewById(R.id.koll_tab_shop);

        mFriendsTab.setOnClickListener(v -> selectTab(0));
        mProfileTab.setOnClickListener(v -> selectTab(1));
        mShopTab.setOnClickListener(v -> selectTab(2));

        applyTheme(view);
        selectTab(mSelectedTab);
    }

    private void selectTab(int index) {
        mSelectedTab = index;
        getChildFragmentManager().beginTransaction()
                .replace(R.id.koll_tab_container, tabFragmentFor(index), "tab_" + index)
                .commit();
        updateTabColors();
    }

    private Fragment tabFragmentFor(int index) {
        switch (index) {
            case 0: return new KollegenFriendsFragment();
            case 1: return new KollegenProfileFragment();
            default: return new KollegenShopFragment();
        }
    }

    private void updateTabColors() {
        int[] pal = KollegenTheme.palette();
        Button[] tabs = new Button[]{mFriendsTab, mProfileTab, mShopTab};
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] == null) continue;
            tabs[i].setTextColor(pal[i == mSelectedTab ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
            tabs[i].setBackgroundColor(pal[KollegenTheme.PANEL2]);
        }
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        View tabBar = view.findViewById(R.id.koll_tab_bar);
        if (tabBar != null) tabBar.setBackgroundColor(KollegenTheme.color(KollegenTheme.PANEL2));
        updateTabColors();
    }
}