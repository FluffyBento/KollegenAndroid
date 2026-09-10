package net.kdt.pojavlaunch.kollegen;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
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
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(12), dp(8), dp(12), dp(8));
        header.setBackgroundColor(KollegenTheme.color(KollegenTheme.PANEL));

        TextView title = new TextView(requireContext());
        title.setText("🎮 Clicker");
        title.setTextColor(KollegenTheme.color(KollegenTheme.TEXT));
        title.setTextSize(16);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button closeBtn = new Button(requireContext());
        closeBtn.setText("✕");
        closeBtn.setTextColor(KollegenTheme.color(KollegenTheme.MUTED));
        closeBtn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        header.addView(closeBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        WebView webView = new WebView(requireContext());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://kollegen.me/clicker");

        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dialog.setContentView(root);
        dialog.setOnDismissListener(d -> {
            webView.stopLoading();
            webView.destroy();
        });
        dialog.show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
