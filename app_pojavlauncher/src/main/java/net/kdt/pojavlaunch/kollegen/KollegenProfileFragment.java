package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
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
        mName.setText(name.isEmpty() ? getString(R.string.kollegen_unknown_user) : name);
        mStatus.setText(KollegenApi.optString(obj, "code"));
        int points = obj.optInt("points", 0);
        int level = obj.optInt("level", 0);
        int total = obj.optInt("points_total", points);
        mPoints.setText(getString(R.string.kollegen_points, points));
        mLevel.setText(getString(R.string.kollegen_level, level));
        mCode.setText("ID: " + KollegenApi.optString(obj, "code"));
        JSONObject profile = obj.optJSONObject("profile");
        if (profile != null) {
            String bio = profile.optString("bio", "");
            if (!bio.isEmpty()) {
                mBio.setText(bio);
                mBio.setVisibility(View.VISIBLE);
            }
        }
        JSONArray cosmetics = obj.optJSONArray("cosmetics");
        String equipped = formatEquipped(obj.optJSONObject("equipped"));
        mEquipped.setText(equipped.isEmpty() ? getString(R.string.kollegen_nothing_equipped) : equipped);
        loadAvatar(obj);
        applyTheme(getView());
    }

    private String formatEquipped(JSONObject equipped) {
        if (equipped == null) return "";
        StringBuilder sb = new StringBuilder();
        JSONArray names = equipped.names();
        if (names == null) return "";
        for (int i = 0; i < names.length(); i++) {
            String key = names.optString(i);
            String value = equipped.optString(key, "");
            if (value.isEmpty()) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append(value);
        }
        return sb.toString();
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
}