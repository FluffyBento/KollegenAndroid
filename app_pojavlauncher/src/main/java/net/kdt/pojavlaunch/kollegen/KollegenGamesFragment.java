package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;

public class KollegenGamesFragment extends Fragment {

    public KollegenGamesFragment() {
        super(R.layout.fragment_kollegen_games);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.koll_game_card_mc).setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null));
        view.findViewById(R.id.koll_game_card_world).setOnClickListener(v ->
                showComingSoon());
        view.findViewById(R.id.koll_game_card_clicker).setOnClickListener(v ->
                showClicker());
        applyTheme(view);
    }

    private void showComingSoon() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.koll_coming_soon_title)
                .setMessage(R.string.koll_coming_soon_msg)
                .setPositiveButton(R.string.koll_ok, (d, w) -> {})
                .show();
    }

    @SuppressWarnings("deprecation")
    private void showClicker() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.koll_tab_container, new KollegenClickerFragment(), "clicker")
                .addToBackStack(null)
                .commit();
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        setTextColor(view, R.id.koll_games_title, KollegenTheme.ACCENT);
        setTextColor(view, R.id.koll_games_subtitle, KollegenTheme.MUTED);
        setTextColor(view, R.id.koll_game_title_mc, KollegenTheme.TEXT);
        setTextColor(view, R.id.koll_game_title_world, KollegenTheme.TEXT);
        setTextColor(view, R.id.koll_game_title_clicker, KollegenTheme.TEXT);
        setTextColor(view, R.id.koll_game_desc_mc, KollegenTheme.MUTED);
        setTextColor(view, R.id.koll_game_desc_world, KollegenTheme.MUTED);
        setTextColor(view, R.id.koll_game_desc_clicker, KollegenTheme.MUTED);
        setCardBackground(view, R.id.koll_game_card_mc);
        setCardBackground(view, R.id.koll_game_card_world);
        setCardBackground(view, R.id.koll_game_card_clicker);
    }

    private void setTextColor(View root, int id, int paletteIndex) {
        View v = root.findViewById(id);
        if (v instanceof TextView) ((TextView) v).setTextColor(KollegenTheme.color(paletteIndex));
    }

    private void setCardBackground(View root, int id) {
        View v = root.findViewById(id);
        if (v != null) v.setBackground(KollegenTheme.panelBackground());
    }
}
