package net.kdt.pojavlaunch.kollegen;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KollegenFriendsFragment extends Fragment {
    private RecyclerView mList;
    private TextView mEmpty;
    private Button mLoginButton;
    private AlertDialog mDialog;
    private FriendAdapter mAdapter;

    public KollegenFriendsFragment() {
        super(R.layout.fragment_kollegen_friends);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList = view.findViewById(R.id.koll_friends_list);
        mEmpty = view.findViewById(R.id.koll_friends_empty);
        mLoginButton = view.findViewById(R.id.koll_friends_login);
        Button mAddButton = view.findViewById(R.id.koll_friends_add);
        EditText mCodeInput = view.findViewById(R.id.koll_friends_code);

        mList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new FriendAdapter();
        mList.setAdapter(mAdapter);

        mLoginButton.setOnClickListener(v -> KollegenSession.login(requireActivity(), this::reload));
        mAddButton.setOnClickListener(v -> {
            String code = mCodeInput.getText() == null ? "" : mCodeInput.getText().toString().trim().toUpperCase();
            if (code.length() < 6) {
                Toast.makeText(requireContext(), R.string.kollegen_friend_code_short, Toast.LENGTH_SHORT).show();
                return;
            }
            addFriend(code);
        });

        applyTheme(view);
        KollegenKit.ensureCatalog(null);
        reload();
    }

    private void reload() {
        if (getView() == null) return;
        KollegenSession.check(requireActivity(), () -> {
            if (getView() == null) return;
            if (KollegenSession.isLoggedIn()) {
                mLoginButton.setVisibility(View.GONE);
                loadFriends();
            } else {
                mLoginButton.setVisibility(View.VISIBLE);
                mAdapter.set(new ArrayList<>());
                mList.setVisibility(View.GONE);
                mEmpty.setText(R.string.kollegen_login_prompt);
                mEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadFriends() {
        KollegenApi.get("/api/profil/friends", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (getView() == null) return;
                JSONArray arr = KollegenApi.optArray(json, "");
                List<JSONObject> items = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) items.add(arr.optJSONObject(i));
                }
                mAdapter.set(items);
                mList.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
                mEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                mEmpty.setText(R.string.kollegen_no_friends);
                listenFriendRequests();
            }

            @Override
            public void onError(String message) {
                if (getView() == null) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenFriendRequests() {
        if (getView() == null || !KollegenSession.isLoggedIn()) return;
        KollegenApi.get("/api/profil/friend-requests", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (getView() == null) return;
                JSONArray arr = KollegenApi.optArray(json, "");
                if (arr == null || arr.length() == 0) return;
                showRequestsDialog(arr);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void showRequestsDialog(JSONArray requests) {
        if (mDialog != null && mDialog.isShowing()) return;
        String[] names = new String[requests.length()];
        for (int i = 0; i < requests.length(); i++) {
            JSONObject req = requests.optJSONObject(i);
            String from = req != null ? req.optString("name", "") : "";
            if (from.isEmpty() && req != null) from = req.optString("username", req.optString("discordName", ""));
            names[i] = from;
        }
        if (requests.length() == 1) {
            JSONObject req = requests.optJSONObject(0);
            String fromId = req != null ? req.optString("from_id", req.optString("discordId", "")) : "";
            showSingleRequest(fromId, names[0]);
            return;
        }
        mDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.kollegen_friend_requests)
                .setItems(names, (d, which) -> {
                    JSONObject req = requests.optJSONObject(which);
                    String fromId = req != null ? req.optString("from_id", req.optString("discordId", "")) : "";
                    showSingleRequest(fromId, names[which]);
                })
                .setNeutralButton(R.string.kollegen_close, (d, w) -> {})
                .setCancelable(true)
                .show();
    }

    private void showSingleRequest(String fromId, String fromName) {
        mDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.kollegen_friend_request_from + " " + fromName)
                .setPositiveButton(R.string.global_yes, (d, w) -> respondRequest(fromId, true))
                .setNegativeButton(R.string.global_no, (d, w) -> respondRequest(fromId, false))
                .setNeutralButton(R.string.kollegen_close, (d, w) -> {})
                .setCancelable(true)
                .show();
    }

    private void respondRequest(String fromId, boolean accept) {
        if (fromId.isEmpty()) return;
        String path = accept ? "/api/profil/friend-accept" : "/api/profil/friend-decline";
        JSONObject body = new JSONObject();
        try {
            body.put("from_id", fromId);
        } catch (Exception e) {
            return;
        }
        KollegenApi.post(path, body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(requireContext(), accept ? R.string.kollegen_friend_added : R.string.kollegen_friend_declined, Toast.LENGTH_SHORT).show();
                    reload();
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null && isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFriend(String code) {
        JSONObject body = new JSONObject();
        try {
            body.put("code", code);
        } catch (Exception e) {
            return;
        }
        KollegenApi.post("/api/profil/friend-add", body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(requireContext(), R.string.kollegen_friend_added, Toast.LENGTH_SHORT).show();
                    reload();
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null && isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        View header = view.findViewById(R.id.koll_friends_header);
        if (header != null) header.setBackgroundColor(KollegenTheme.color(KollegenTheme.PANEL));
        int[] pal = KollegenTheme.palette();
        if (mLoginButton != null) {
            mLoginButton.setBackground(KollegenTheme.buttonBackground(KollegenTheme.ACCENT, KollegenTheme.ACCENT2));
            mLoginButton.setTextColor(pal[KollegenTheme.BG]);
        }
        Button add = view.findViewById(R.id.koll_friends_add);
        if (add != null) {
            add.setBackground(KollegenTheme.buttonBackground(KollegenTheme.PANEL2, KollegenTheme.ACCENT2));
            add.setTextColor(pal[KollegenTheme.TEXT]);
        }
        EditText code = view.findViewById(R.id.koll_friends_code);
        if (code != null) {
            code.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
            code.setTextColor(pal[KollegenTheme.TEXT]);
            code.setHintTextColor(pal[KollegenTheme.MUTED]);
        }
        if (mEmpty != null) {
            mEmpty.setTextColor(pal[KollegenTheme.MUTED]);
        }
    }

    private void openFriendProfile(final JSONObject f) {
        int[] pal = KollegenTheme.palette();
        String name = f.optString("name", "Unbekannt");
        final String did = f.optString("discordId", f.optString("id", ""));
        final String code = f.optString("code", "");
        JSONObject prof = f.optJSONObject("profile");
        final String bannerData = prof != null ? prof.optString("banner_data_url", "") : "";
        final String avatarData = prof != null ? prof.optString("avatar_data_url", "") : "";

        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(4), dp(24), dp(4));

        LinearLayout bannerWrap = new LinearLayout(requireContext());
        bannerWrap.setOrientation(LinearLayout.VERTICAL);
        box.addView(bannerWrap);

        LinearLayout top = new LinearLayout(requireContext());
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, dp(6), 0, 0);

        final FrameLayout avatarHold = KollegenKit.avatarHolder(requireContext(), null, 56, pal);
        top.addView(avatarHold);

        LinearLayout metaCol = new LinearLayout(requireContext());
        metaCol.setOrientation(LinearLayout.VERTICAL);
        metaCol.setPadding(dp(12), 0, 0, 0);

        final TextView title = new TextView(requireContext());
        title.setText(name);
        title.setTextColor(pal[KollegenTheme.TEXT]);
        title.setTextSize(17);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        metaCol.addView(title);

        final TextView meta = new TextView(requireContext());
        meta.setTextColor(pal[KollegenTheme.MUTED]);
        meta.setTextSize(13);
        metaCol.addView(meta);
        top.addView(metaCol);

        final TextView bio = new TextView(requireContext());
        bio.setTextColor(pal[KollegenTheme.TEXT]);
        bio.setTextSize(14);
        bio.setPadding(0, dp(10), 0, 0);
        box.addView(top);
        box.addView(bio);

        if (!avatarData.isEmpty()) {
            KollegenAvatar.loadDataUrl((ImageView) avatarHold.getChildAt(0), avatarData);
        } else {
            KollegenAvatar.loadMcHead((ImageView) avatarHold.getChildAt(0), f.optString("uuid", "MHF_Steve"));
        }
        if (prof != null) {
            String bioText = prof.optString("bio", "").trim();
            if (!bioText.isEmpty()) bio.setText(bioText);
        }

        LinearLayout equippedRow = new LinearLayout(requireContext());
        equippedRow.setOrientation(LinearLayout.HORIZONTAL);
        equippedRow.setPadding(0, dp(10), 0, 0);
        box.addView(equippedRow);

        final boolean online = f.optBoolean("online", false);
        final int level = f.optInt("level", 0);
        final boolean isFriend = f.optBoolean("isFriend", true);
        KollegenKit.ensureCatalog(() -> {
            if (!isAdded() || getActivity() == null) return;
            JSONObject eqResolved = KollegenKit.resolveEquippedTree(f);
            int accent = KollegenKit.accent(eqResolved, pal[KollegenTheme.ACCENT]);

            avatarHold.setBackground(KollegenKit.avatarBackground(requireContext(), eqResolved, pal));

            JSONObject pbg = eqResolved.optJSONObject("profile_bg");
            if (pbg != null) {
                JSONObject pd = pbg.optJSONObject("data");
                String pbGrad = pd != null ? pd.optString("gradient", "") : "";
                if (!pbGrad.isEmpty()) {
                    box.setBackground(KollegenKit.gradient(requireContext(), pbGrad, pal[KollegenTheme.PANEL]));
                }
            }

            String badge = KollegenKit.badgeIcon(eqResolved);
            String stick = KollegenKit.stickerIcon(eqResolved);
            StringBuilder nm = new StringBuilder();
            if (!badge.isEmpty()) nm.append(badge).append(" ");
            nm.append(KollegenKit.applyTitle(eqResolved, name));
            if (!stick.isEmpty()) nm.append(" ").append(stick);
            title.setText(nm.toString());
            title.setTextColor(accent);
            if (!badge.isEmpty()) title.setTextColor(KollegenKit.badgeColor(eqResolved, accent));
            KollegenKit.applyNameFont(title, KollegenKit.nameFont(eqResolved));

            String server = f.optString("server", "");
            StringBuilder sb = new StringBuilder();
            if (online) {
                sb.append(getString(R.string.kollegen_online));
                if (!server.isEmpty()) sb.append(" \u00b7 ").append(server);
            } else {
                sb.append(getString(R.string.kollegen_offline));
            }
            if (level > 0) sb.append(" \u00b7 ").append(getString(R.string.kollegen_level, level));
            if (!code.isEmpty()) sb.append(" \u00b7 ").append(getString(R.string.kollegen_friend_code_chip, code));
            if (isFriend) sb.append(" \u00b7 ").append(getString(R.string.kollegen_friend_chip));
            meta.setText(sb.toString());
            meta.setTextColor(pal[online ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);

            String bannerGrad = "";
            JSONObject banner = eqResolved.optJSONObject("banner");
            if (banner != null) {
                JSONObject bd = banner.optJSONObject("data");
                bannerGrad = bd != null ? bd.optString("gradient", "") : "";
            }
            if (!bannerData.isEmpty()) {
                bannerWrap.addView(KollegenKit.bannerImage(requireContext(), bannerData, pal));
            } else if (!bannerGrad.isEmpty()) {
                bannerWrap.addView(KollegenKit.bannerView(requireContext(), gradientObj(bannerGrad), pal));
            } else {
                bannerWrap.setVisibility(View.GONE);
            }

            boolean hasPreview = KollegenKit.renderEquippedRow(requireContext(), equippedRow, eqResolved, pal, dp(42));
            if (!hasPreview) addEquippedText(equippedRow, eqResolved);
        });

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(14), 0, 0);

        Button dm = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_dm));
        dm.setOnClickListener(v -> {
            KollegenChatFragment chat = new KollegenChatFragment(did, name);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.koll_tab_container, chat, "chat")
                    .addToBackStack(null)
                    .commit();
        });

        Button call = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_call));
        call.setOnClickListener(v -> KollegenCall.startDirect(requireContext(), did, name));

        Button inv = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_inventory));
        inv.setOnClickListener(v -> showInventory(code, name));

        actions.addView(dm);
        actions.addView(call);
        actions.addView(inv);
        box.addView(actions);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(box)
                .setNegativeButton(R.string.kollegen_close, null)
                .create();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawable(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        dlg.show();
        mDialog = dlg;
    }

    private static JSONObject gradientObj(String gradient) {
        JSONObject d = new JSONObject();
        try { d.put("gradient", gradient); } catch (Exception ignored) {}
        return d;
    }

    private void addEquippedText(LinearLayout row, JSONObject eqResolved) {
        int[] pal = KollegenTheme.palette();
        boolean any = false;
        StringBuilder sb = new StringBuilder();
        String[] cats = {"title", "badge", "avatar_theme", "avatar_frame", "banner", "profile_bg", "profil_stil", "font", "sticker", "name_color"};
        for (String c : cats) {
            String n = KollegenKit.equippedName(eqResolved, c);
            if (!n.isEmpty()) {
                if (sb.length() > 0) sb.append(" \u00b7 ");
                sb.append(n);
                any = true;
            }
        }
        if (!any) return;
        TextView t = new TextView(requireContext());
        t.setText(sb.toString());
        t.setTextColor(pal[KollegenTheme.MUTED]);
        t.setTextSize(12);
        t.setPadding(0, dp(6), 0, 0);
        row.addView(t);
    }

    private void showInventory(String code, String name) {
        if (code.isEmpty()) {
            Toast.makeText(requireContext(), R.string.kollegen_inventory_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        KollegenApi.get("/api/profil/profile-view?code=" + KollegenApi.encode(code), new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded()) return;
                int[] pal = KollegenTheme.palette();
                LinearLayout box = new LinearLayout(requireContext());
                box.setOrientation(LinearLayout.VERTICAL);
                box.setPadding(dp(24), dp(12), dp(24), dp(4));
                TextView h = new TextView(requireContext());
                h.setText(getString(R.string.kollegen_inventory));
                h.setTextColor(pal[KollegenTheme.TEXT]);
                h.setTextSize(16);
                h.setTypeface(Typeface.DEFAULT_BOLD);
                box.addView(h);
                TextView l = new TextView(requireContext());
                l.setText(name);
                l.setTextColor(pal[KollegenTheme.MUTED]);
                l.setTextSize(13);
                l.setPadding(0, 0, 0, dp(8));
                box.addView(l);
                KollegenKit.ensureCatalog(() -> {
                    if (!isAdded()) return;
                    JSONArray owned = json instanceof JSONObject ? ((JSONObject) json).optJSONArray("owned") : null;
                    KollegenKit.renderInventory(requireContext(), box, null, owned, pal, 56, getString(R.string.kollegen_inventory_empty));
                });
                new AlertDialog.Builder(requireContext())
                        .setView(box)
                        .setNegativeButton(R.string.kollegen_close, null)
                        .show();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.Holder> {
        private final List<JSONObject> mItems = new ArrayList<>();

        public void set(List<JSONObject> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kollegen_friend, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            final JSONObject item = mItems.get(position);
            String rawName = item.optString("name", "");
            if (rawName.isEmpty()) rawName = item.optString("username", item.optString("discordName", ""));
            final String name = rawName;
            holder.mName.setText(name);
            int level = item.optInt("level", 0);
            final boolean online = item.optBoolean("online", false);
            String metaText = online ? getString(R.string.kollegen_online) : getString(R.string.kollegen_offline);
            if (level > 0) metaText += " \u00b7 " + getString(R.string.kollegen_level, level);
            holder.mMeta.setText(metaText);
            holder.mLevel.setText(level > 0 ? "Lv " + level : "");
            int[] pal = KollegenTheme.palette();
            holder.mName.setTextColor(pal[online ? KollegenTheme.TEXT : KollegenTheme.MUTED]);
            holder.mMeta.setTextColor(pal[online ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
            holder.mLevel.setTextColor(pal[KollegenTheme.MUTED]);

            KollegenAvatar.loadMcHead(holder.mAvatar, "MHF_Steve");
            JSONObject prof = item.optJSONObject("profile");
            final String avatarData = prof != null ? prof.optString("avatar_data_url", "") : "";
            if (!avatarData.isEmpty()) KollegenAvatar.loadDataUrl(holder.mAvatar, avatarData);

            KollegenKit.ensureCatalog(() -> {
                if (!isAdded()) return;
                JSONObject eqResolved = KollegenKit.resolveEquippedTree(item);
                holder.mName.setText(KollegenKit.applyTitle(eqResolved, name));
                holder.mName.setTextColor(KollegenKit.accent(eqResolved, pal[online ? KollegenTheme.TEXT : KollegenTheme.MUTED]));
            });

            holder.itemView.setOnClickListener(v -> openFriendProfile(item));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        private class Holder extends RecyclerView.ViewHolder {
            final TextView mName;
            final TextView mMeta;
            final TextView mLevel;
            final ImageView mAvatar;

            Holder(@NonNull View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.koll_friend_name);
                mMeta = itemView.findViewById(R.id.koll_friend_meta);
                mLevel = itemView.findViewById(R.id.koll_friend_level);
                mAvatar = itemView.findViewById(R.id.koll_friend_avatar);
            }
        }
    }
}