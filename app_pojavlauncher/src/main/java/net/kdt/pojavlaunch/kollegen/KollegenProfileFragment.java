package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONObject;

public class KollegenProfileFragment extends Fragment {
    private View mGuestView;
    private View mUserView;
    private TextView mName;
    private TextView mStatus;
    private TextView mPoints;
    private TextView mLevel;
    private TextView mCode;
    private TextView mBio;
    private TextView mEquipped;
    private LinearLayout mEquippedRow;
    private LinearLayout mBannerHolder;
    private LinearLayout mUserBox;

    public KollegenProfileFragment() {
        super(R.layout.fragment_kollegen_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mGuestView = view.findViewById(R.id.koll_profile_guest);
        mUserView = view.findViewById(R.id.koll_profile_user);
        mName = view.findViewById(R.id.koll_profile_name);
        mStatus = view.findViewById(R.id.koll_profile_status);
        mPoints = view.findViewById(R.id.koll_profile_points);
        mLevel = view.findViewById(R.id.koll_profile_level);
        mCode = view.findViewById(R.id.koll_profile_code);
        mBio = view.findViewById(R.id.koll_profile_bio);
        mEquipped = view.findViewById(R.id.koll_profile_equipped);
        mEquippedRow = view.findViewById(R.id.koll_profile_equipped_row);
        mBannerHolder = view.findViewById(R.id.koll_profile_banner_holder);
        mUserBox = view.findViewById(R.id.koll_profile_user);

        view.findViewById(R.id.koll_profile_login).setOnClickListener(v -> KollegenSession.login(requireActivity(), this::reload));
        view.findViewById(R.id.koll_profile_logout).setOnClickListener(v -> KollegenSession.logout(requireActivity(), this::reload));

        applyTheme(view);
        reload();
    }

    private void reload() {
        if (getView() == null) return;
        KollegenSession.check(requireActivity(), () -> {
            if (getView() == null) return;
            if (KollegenSession.isLoggedIn()) {
                loadMe();
            } else {
                mUserView.setVisibility(View.GONE);
                mGuestView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadMe() {
        KollegenApi.get("/api/profil/me", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (getView() == null) return;
                if (!(json instanceof JSONObject)) return;
                JSONObject obj = (JSONObject) json;
                JSONObject user = obj.optJSONObject("user");
                if (user == null) {
                    mUserView.setVisibility(View.GONE);
                    mGuestView.setVisibility(View.VISIBLE);
                    return;
                }
                mGuestView.setVisibility(View.GONE);
                mUserView.setVisibility(View.VISIBLE);
                bindMe(obj);
            }

            @Override
            public void onError(String message) {
                if (getView() == null) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindMe(JSONObject obj) {
        String mcName = KollegenApi.optString(obj, "mcName");
        String name = !mcName.isEmpty() ? mcName : KollegenApi.optString(obj, "name");
        JSONObject profile = obj.optJSONObject("profile");
        int[] pal = KollegenTheme.palette();

        mEquipped.setText("");
        mEquippedRow.removeAllViews();
        mBannerHolder.removeAllViews();
        mUserBox.setBackgroundColor(pal[KollegenTheme.PANEL]);

        mName.setText(name.isEmpty() ? getString(R.string.kollegen_unknown_user) : name);
        mName.setTextColor(pal[KollegenTheme.TEXT]);
        mStatus.setText(KollegenApi.optString(obj, "code"));
        int points = obj.optInt("points", 0);
        int level = obj.optInt("level", 0);
        mPoints.setText(getString(R.string.kollegen_points, points));
        mLevel.setText(getString(R.string.kollegen_level, level));
        mCode.setText("ID: " + KollegenApi.optString(obj, "code"));
        mBio.setVisibility(View.GONE);
        if (profile != null) {
            String bio = profile.optString("bio", "");
            if (!bio.isEmpty()) {
                mBio.setText(bio);
                mBio.setVisibility(View.VISIBLE);
            }
        }

        loadAvatar(obj);

        KollegenKit.ensureCatalog(() -> {
            if (!isAdded() || getView() == null) return;
            JSONObject eqResolved = KollegenKit.resolveEquippedTree(obj);
            int accent = KollegenKit.accent(eqResolved, pal[KollegenTheme.TEXT]);
            mName.setText(KollegenKit.applyTitle(eqResolved, name.isEmpty() ? getString(R.string.kollegen_unknown_user) : name));
            mName.setTextColor(accent);

            String bannerGrad = "";
            JSONObject banner = eqResolved.optJSONObject("banner");
            if (banner != null) {
                JSONObject bd = banner.optJSONObject("data");
                bannerGrad = bd != null ? bd.optString("gradient", "") : "";
            }
            applyBanner(bannerGrad, accent);

            StringBuilder equippedText = new StringBuilder();
            String[] textCats = {"title", "badge", "name_color", "font"};
            for (String c : textCats) {
                String n = KollegenKit.equippedName(eqResolved, c);
                if (!n.isEmpty()) {
                    if (equippedText.length() > 0) equippedText.append("\n");
                    equippedText.append(n);
                }
            }
            mEquipped.setText(equippedText.toString());
            KollegenKit.renderEquippedRow(requireContext(), mEquippedRow, eqResolved, pal, dp(48));
            applyTheme(getView());
        });
    }

    private void applyBanner(String gradient, int fallback) {
        if (mBannerHolder == null) return;
        if (gradient.isEmpty() && fallback == KollegenTheme.palette()[KollegenTheme.TEXT]) {
            mBannerHolder.setVisibility(View.GONE);
            return;
        }
        View banner = KollegenKit.bannerView(requireContext(), gradientObj(gradient), KollegenTheme.palette());
        mBannerHolder.addView(banner);
        mBannerHolder.setVisibility(View.VISIBLE);
    }

    private static JSONObject gradientObj(String gradient) {
        JSONObject d = new JSONObject();
        try { d.put("gradient", gradient); } catch (Exception ignored) {}
        return d;
    }

    private void loadAvatar(JSONObject obj) {
        ImageView avatar = getView() == null ? null : getView().findViewById(R.id.koll_profile_avatar);
        if (avatar == null) return;
        String mcName = KollegenApi.optString(obj, "mcName");
        JSONObject profile = obj.optJSONObject("profile");
        String dataUrl = profile != null ? profile.optString("avatar_data_url", "") : "";
        if (!dataUrl.isEmpty()) {
            KollegenAvatar.loadDataUrl(avatar, dataUrl);
        } else if (!mcName.isEmpty()) {
            KollegenAvatar.loadMcHead(avatar, mcName);
        }
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        int[] pal = KollegenTheme.palette();
        TextView[] texts = new TextView[]{mName, mStatus, mPoints, mLevel, mCode, mBio, mEquipped};
        for (TextView t : texts) {
            if (t != null) t.setTextColor(pal[KollegenTheme.TEXT]);
        }
        TextView equippedTitle = view.findViewById(R.id.koll_profile_equipped_title);
        if (equippedTitle != null) equippedTitle.setTextColor(pal[KollegenTheme.TEXT]);
        Button login = view.findViewById(R.id.koll_profile_login);
        if (login != null) {
            login.setBackground(KollegenTheme.buttonBackground(KollegenTheme.ACCENT, KollegenTheme.ACCENT2));
            login.setTextColor(pal[KollegenTheme.BG]);
        }
        Button logout = view.findViewById(R.id.koll_profile_logout);
        if (logout != null) {
            logout.setBackground(KollegenTheme.buttonBackground(KollegenTheme.PANEL2, KollegenTheme.ACCENT2));
            logout.setTextColor(pal[KollegenTheme.TEXT]);
        }
        TextView guest = view.findViewById(R.id.koll_profile_guest_text);
        if (guest != null) guest.setTextColor(pal[KollegenTheme.MUTED]);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}