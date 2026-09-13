package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.KollegenUpdater;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

public class KollegenSettingsFragment extends Fragment {

    public KollegenSettingsFragment() {
        super(R.layout.fragment_kollegen_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.koll_settings_theme).setOnClickListener(v -> openThemeDialog());
        view.findViewById(R.id.koll_settings_more).setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), LauncherPreferenceFragment.class, "kollegen_prefs", null));
        view.findViewById(R.id.koll_settings_update).setOnClickListener(v ->
                KollegenUpdater.checkNow(requireActivity()));
        applyTheme(view);
    }

    private void openThemeDialog() {
        String[] names = KollegenTheme.themeNames();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.koll_settings_pick_theme)
                .setSingleChoiceItems(names, currentThemeIndex(names), (d, which) -> {
                    KollegenTheme.setTheme(names[which]);
                    d.dismiss();
                    requireActivity().recreate();
                })
                .show();
    }

    private int currentThemeIndex(String[] names) {
        String current = KollegenTheme.currentName();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(current)) return i;
        }
        return 0;
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        TextView[] headings = {
                view.findViewById(R.id.koll_settings_section_design),
                view.findViewById(R.id.koll_settings_section_launcher),
                view.findViewById(R.id.koll_settings_section_about)
        };
        for (TextView heading : headings) {
            if (heading != null) heading.setTextColor(KollegenTheme.color(KollegenTheme.ACCENT));
        }
        Button theme = view.findViewById(R.id.koll_settings_theme);
        Button more = view.findViewById(R.id.koll_settings_more);
        Button update = view.findViewById(R.id.koll_settings_update);
        if (theme != null) {
            theme.setBackground(KollegenTheme.buttonBackground(KollegenTheme.PANEL2, KollegenTheme.ACCENT2));
            theme.setTextColor(KollegenTheme.color(KollegenTheme.TEXT));
        }
        if (more != null) {
            more.setBackground(KollegenTheme.buttonBackground(KollegenTheme.PANEL2, KollegenTheme.ACCENT2));
            more.setTextColor(KollegenTheme.color(KollegenTheme.TEXT));
        }
        if (update != null) {
            update.setBackground(KollegenTheme.buttonBackground(KollegenTheme.PANEL2, KollegenTheme.ACCENT2));
            update.setTextColor(KollegenTheme.color(KollegenTheme.TEXT));
        }
        TextView about = view.findViewById(R.id.koll_settings_about_text);
        if (about != null) about.setTextColor(KollegenTheme.color(KollegenTheme.MUTED));
    }
}