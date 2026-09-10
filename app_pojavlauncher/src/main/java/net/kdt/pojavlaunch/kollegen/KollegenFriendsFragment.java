package net.kdt.pojavlaunch.kollegen;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
            JSONObject item = mItems.get(position);
            String name = item.optString("name", "");
            if (name.isEmpty()) name = item.optString("username", item.optString("discordName", ""));
            holder.mName.setText(name);
            int level = item.optInt("level", 0);
            boolean online = item.optBoolean("online", false);
            holder.mMeta.setText(online ? R.string.kollegen_online : R.string.kollegen_offline);
            holder.mLevel.setText(level > 0 ? "Lv " + level : "");
            int[] pal = KollegenTheme.palette();
            holder.mName.setTextColor(pal[online ? KollegenTheme.TEXT : KollegenTheme.MUTED]);
            holder.mMeta.setTextColor(pal[online ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
            holder.mLevel.setTextColor(pal[KollegenTheme.MUTED]);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        private class Holder extends RecyclerView.ViewHolder {
            final TextView mName;
            final TextView mMeta;
            final TextView mLevel;

            Holder(@NonNull View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.koll_friend_name);
                mMeta = itemView.findViewById(R.id.koll_friend_meta);
                mLevel = itemView.findViewById(R.id.koll_friend_level);
            }
        }
    }
}